package Event.Event_booking.service;

import Event.Event_booking.Repository.UserRepository;
import Event.Event_booking.dto.UserRegistrationDTO;
import Event.Event_booking.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public void userRegistration(UserRegistrationDTO registrationDTO){
        User user = new User();
        user.setName(registrationDTO.getName());
        user.setEmail(registrationDTO.getEmail());
        user.setPasswordHash(registrationDTO.getPasswordHash());
        userRepository.save(user);
    }
}
