package Event.Event_booking.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "waitlist",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_waitlist_user_event", columnNames = {"user_id", "event_id"})
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "joined_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime joinedAt = OffsetDateTime.now();

    @Column(name = "notified", nullable = false)
    @Builder.Default
    private boolean notified = false;
}