package Event.Event_booking.mapper;

import Event.Event_booking.dto.UserLoginDTO;
import Event.Event_booking.entity.User;

public class UserLoginMapper {
    public static User toEntity(UserLoginDTO userLoginDTO){
        User user = new User();
        user.setEmail(userLoginDTO.getEmail());
        user.setPasswordHash(userLoginDTO.getPasswordHash());
        return user;
    }

    public static UserLoginDTO toDto(User user){
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setEmail(user.getEmail());
        userLoginDTO.setPasswordHash(user.getPasswordHash());
        return userLoginDTO;
    }

}
