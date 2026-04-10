package Event.Event_booking.controller;

import Event.Event_booking.dto.UserLoginDTO;
import Event.Event_booking.dto.UserRegistrationDTO;
import Event.Event_booking.entity.User;
import Event.Event_booking.security.JwtTokenService;
import Event.Event_booking.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthControllers {
    private final UserService userService;
    private final JwtTokenService jwtTokenService;


    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody UserRegistrationDTO request) {
        User user = userService.userRegistration(request);
        
        Map<String, String> tokens = jwtTokenService.generateTokens(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserLoginDTO loginrequest) {
        User user = userService.login(loginrequest);
        
        Map<String, String> tokens = jwtTokenService.generateTokens(user);
        
        return ResponseEntity.ok(Map.of(
            "token", tokens.get("accessToken"),
            "refreshToken", tokens.get("refreshToken")
        ));
    }

    /**
     * Refresh the access token using a valid refresh token
     * @param request a map containing the refreshToken
     * @return a map containing the new accessToken
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (!jwtTokenService.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired refresh token"));
        }

        String newAccessToken = jwtTokenService.generateAccessTokenFromRefreshToken(refreshToken);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (!jwtTokenService.logoutRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired refresh token"));
        }

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
