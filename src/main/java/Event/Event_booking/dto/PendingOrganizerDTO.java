package Event.Event_booking.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * Projection returned to admins reviewing pending organizer requests.
 * Contains only non-sensitive fields — password hash is intentionally excluded.
 */
@Getter
@Builder
public class PendingOrganizerDTO {
    private Long id;
    private String name;
    private String email;
    private OffsetDateTime createdAt;
}

