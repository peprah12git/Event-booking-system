package Event.Event_booking.mapper;

import Event.Event_booking.dto.UserRegistrationDTO;
import Event.Event_booking.entity.User;

public class UserRegistrationMapper {
    // DTO to entity method
    public static User toEntity(UserRegistrationDTO registrationDTo) {
        User user = new User();
        user.setName(registrationDTo.getName());
        user.setEmail(registrationDTo.getEmail());
        user.setPasswordHash(registrationDTo.getPasswordHash());
        return user;

    }
    public static UserRegistrationDTO toDTO(User user){
        UserRegistrationDTO userRegistrationDTo = new UserRegistrationDTO();
        userRegistrationDTo.setName(user.getName());
        userRegistrationDTo.setEmail(user.getEmail());
        userRegistrationDTo.setPasswordHash(user.getPasswordHash());
        return userRegistrationDTo;
    }
}

