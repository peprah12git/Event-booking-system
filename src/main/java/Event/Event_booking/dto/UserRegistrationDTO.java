package Event.Event_booking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDTO {
    private String name;
    private String email;
    private String passwordHash;
}
