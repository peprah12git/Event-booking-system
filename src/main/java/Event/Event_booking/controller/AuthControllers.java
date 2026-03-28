package Event.Event_booking.controller;

import Event.Event_booking.dto.UserRegistrationDTO;
import Event.Event_booking.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthControllers {
    private  final UserService userService;

@PostMapping("/register")
    public ResponseEntity <String> registerUser(@RequestBody UserRegistrationDTO request){
    userService.userRegistration(request);
    return ResponseEntity.status(HttpStatus.CREATED).body("Registration Successful");
}


}
