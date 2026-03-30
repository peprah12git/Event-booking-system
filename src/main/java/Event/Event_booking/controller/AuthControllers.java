package Event.Event_booking.controller;

import Event.Event_booking.dto.UserLoginDTO;
import Event.Event_booking.dto.UserRegistrationDTO;
import Event.Event_booking.security.utils.JwtUtil;
import Event.Event_booking.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthControllers {
    private  final UserService userService;
    private  final JwtUtil jwtUtil;

@PostMapping("/register")
    public ResponseEntity <String> registerUser(@Valid @RequestBody UserRegistrationDTO request){
    userService.userRegistration(request);

    return ResponseEntity.status(HttpStatus.CREATED).body("Registration Successful");
}

@PostMapping("/login")
    public  ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserLoginDTO loginrequest){
    String role = userService.login(loginrequest);
    String token = jwtUtil.generateToken(loginrequest.getEmail(), role);
    return ResponseEntity.ok(Map.of("token", token));


    }
}
