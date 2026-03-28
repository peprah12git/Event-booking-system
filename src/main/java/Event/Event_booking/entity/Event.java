package Event.Event_booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "events",
        indexes = {
                @Index(name = "idx_events_organizer", columnList = "organizer_id"),
                @Index(name = "idx_events_date",      columnList = "event_date"),
                @Index(name = "idx_events_category",  columnList = "category_id")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date", nullable = false)
    private OffsetDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "available_tickets", nullable = false)
    private Integer availableTickets;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_code", nullable = false)
    private Currency currency;

    @Column(name = "primary_image_url", length = 500)
    private String primaryImageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Maintained by the {@code trg_events_updated_at} PostgreSQL trigger.
     * The {@link PreUpdate} callback mirrors it on the Java side so the
     * in-memory entity stays consistent within the same session.
     */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}