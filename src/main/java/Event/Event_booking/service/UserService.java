package Event.Event_booking.service;


import Event.Event_booking.repository.RoleRepository;
import Event.Event_booking.repository.UserRepository;
import Event.Event_booking.dto.UserLoginDTO;
import Event.Event_booking.dto.UserRegistrationDTO;
import Event.Event_booking.entity.Role;
import Event.Event_booking.entity.User;
import Event.Event_booking.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class UserService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(JwtUtil jwtUtil, UserRepository userRepository, RoleRepository roleRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;

    }


    public Map<String,String> userRegistration(UserRegistrationDTO registrationDTO){
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
        return Map.of("email", user.getEmail(), "role", attendeeRole.getName());

    }

    public String login(UserLoginDTO loginDTO){
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(loginDTO.getPasswordHash(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
return user.getRole().getName();

    }

    }
