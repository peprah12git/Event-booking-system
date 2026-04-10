package Event.Event_booking.service;


import Event.Event_booking.dto.UserProfileDTO;
import Event.Event_booking.entity.*;
import Event.Event_booking.repository.*;
import Event.Event_booking.dto.UserLoginDTO;
import Event.Event_booking.dto.UserRegistrationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditActionRepository auditActionRepository;
    private final AuditEntityRepository auditEntityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,  AuditLogRepository auditLogRepository, AuditActionRepository auditActionRepository, AuditEntityRepository auditEntityRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditActionRepository = auditActionRepository;
        this.auditEntityRepository = auditEntityRepository;
    }

    /**
     * Registers a new user
     * @param registrationDTO the registration request data
     * @return the created User entity
     * @throws RuntimeException if email already exists or role not found
     */
    public User userRegistration(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        Role attendeeRole = roleRepository.findByName("ATTENDEE")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setName(registrationDTO.getName());
        user.setEmail(registrationDTO.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registrationDTO.getPasswordHash()));
        user.setRole(attendeeRole);
        userRepository.save(user);
        return user;
    }

    /**
     * Authenticates a user and returns the user entity
     * @param loginDTO the login request data
     * @return the authenticated User entity
     * @throws RuntimeException if credentials are invalid
     */
    public User login(UserLoginDTO loginDTO) {
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(loginDTO.getPasswordHash(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        return user;
    }

    /**
     * Returns the profile of the currently authenticated user.
     * Password hash is never included in the response.
     * @param email the email extracted from the JWT (set in SecurityContext)
     * @return UserProfileDTO with id, name, email, role, isOrganizerApproved, createdAt
     * @throws RuntimeException if no user matches the email
     */
    public UserProfileDTO getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserProfileDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .isOrganizerApproved(user.isOrganizerApproved())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Submits a request for the authenticated attendee to become an organizer.
     * Updates role_id to ORGANIZER while keeping is_organizer_approved = FALSE.
     * Records the change in audit_logs (action=UPDATE, entity=User).
     *
     * @param email the authenticated user's email
     * @return confirmation message
     * @throws RuntimeException if user not found, role/audit refs missing,
     *                          or the request was already made / already approved
     */
    @Transactional
    public Map<String, String> requestOrganizerRole(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String currentRoleName = user.getRole().getName();

        // Guard: already an approved organizer
        if ("ORGANIZER".equals(currentRoleName) && user.isOrganizerApproved()) {
            throw new RuntimeException("You are already an approved organizer");
        }

        // Guard: request already pending
        if ("ORGANIZER".equals(currentRoleName)) {
            throw new RuntimeException("Your organizer request is already under review");
        }

        // Capture state BEFORE update for audit trail
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("role", currentRoleName);
        oldValues.put("is_organizer_approved", user.isOrganizerApproved());

        // Update role → ORGANIZER; is_organizer_approved stays false
        Role organizerRole = roleRepository.findByName("ORGANIZER")
                .orElseThrow(() -> new RuntimeException("ORGANIZER role not found in database"));
        user.setRole(organizerRole);
        userRepository.save(user);

        // Capture state AFTER update for audit trail
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("role", "ORGANIZER");
        newValues.put("is_organizer_approved", false);

        // Write audit log entry (action=UPDATE, entity=User)
        AuditAction updateAction = auditActionRepository.findByName("UPDATE")
                .orElseThrow(() -> new RuntimeException("Audit action 'UPDATE' not found in database"));
        AuditEntity userEntity = auditEntityRepository.findByName("User")
                .orElseThrow(() -> new RuntimeException("Audit entity 'User' not found in database"));

        Auditlog auditlog = Auditlog.builder()
                .user(user)
                .action(updateAction)
                .entity(userEntity)
                .recordId(user.getId())
                .oldValues(oldValues)
                .newValues(newValues)
                .build();
        auditLogRepository.save(auditlog);

        return Map.of("message", "Your request to become an organizer has been submitted and is under review");
    }
}
