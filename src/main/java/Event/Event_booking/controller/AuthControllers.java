package Event.Event_booking.controller;

import Event.Event_booking.dto.UserLoginDTO;
import Event.Event_booking.dto.UserRegistrationDTO;
import Event.Event_booking.utils.JwtUtil;
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
    private  final UserService userService;
    private  final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody UserRegistrationDTO request) {
        Map<String, String> userInfo = userService.userRegistration(request);

        String accessToken = jwtUtil.generateToken(userInfo.get("email"), userInfo.get("role"));
        String refreshToken = jwtUtil.generateRefreshToken(userInfo.get("email"), userInfo.get("role"));

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

@PostMapping("/login")
    public  ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserLoginDTO loginrequest){
    String role = userService.login(loginrequest);
    String token = jwtUtil.generateToken(loginrequest.getEmail(), role);
    String refreshToken = jwtUtil.generateRefreshToken(loginrequest.getEmail(), role);
    return ResponseEntity.ok(Map.of("token", token,  "refreshToken", refreshToken));


    }
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (!jwtUtil.isTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired refresh token"));
        }

        String email = jwtUtil.extractEmail(refreshToken);
        String newAccessToken = jwtUtil.generateToken(email, jwtUtil.extractRole(refreshToken));

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}
