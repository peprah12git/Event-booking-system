package Event.Event_booking.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * Read-only view of a user's own profile.
 * Password hash is intentionally excluded.
 */
@Getter
@Builder
public class UserProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private boolean isOrganizerApproved;
    private OffsetDateTime createdAt;
}

