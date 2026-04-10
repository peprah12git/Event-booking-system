package Event.Event_booking.controller;

import Event.Event_booking.dto.UserProfileDTO;
import Event.Event_booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users/profile
     * Returns the authenticated user's profile. Requires a valid Bearer JWT.
     * Password hash is never included in the response.
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        UserProfileDTO profile = userService.getProfileByEmail(email);
        return ResponseEntity.ok(profile);
    }

    /**
     * POST /api/users/request-organizer
     * Submits a request for the logged-in attendee to become an organizer.
     * Role is updated to ORGANIZER immediately; is_organizer_approved remains FALSE
     * until an admin approves the request. Action recorded in audit_logs.
     */
    @PostMapping("/request-organizer")
    public ResponseEntity<Map<String, String>> requestOrganizerRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Map<String, String> response = userService.requestOrganizerRole(email);
        return ResponseEntity.ok(response);
    }
}
