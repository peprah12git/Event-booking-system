package Event.Event_booking.mapper;

import Event.Event_booking.dto.UserRegistrationDTo;
import Event.Event_booking.entity.User;

public class UserRegistrationMapper {
    // DTO to entity method
    public static User toEntity(UserRegistrationDTo registrationDTo) {
        User user = new User();
        user.setName(registrationDTo.getName());
        user.setEmail(registrationDTo.getEmail());
        user.setPasswordHash(registrationDTo.getPasswordHash());
        return user;

    }
    public static UserRegistrationDTo todto(User user){
        UserRegistrationDTo userRegistrationDTo = new UserRegistrationDTo();
        userRegistrationDTo.setName(user.getName());
        userRegistrationDTo.setEmail(user.getEmail());
        userRegistrationDTo.setPasswordHash(user.getPasswordHash());
        return userRegistrationDTo;
    }
}

