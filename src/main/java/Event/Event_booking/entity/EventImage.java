package Event.Event_booking.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "event_images",
        indexes = {
                @Index(name = "idx_ei_event_order", columnList = "event_id, display_order")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_type_id", nullable = false)
    private ImageType imageType;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size_bytes", nullable = false)
    private Integer fileSizeBytes;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Short displayOrder = 0;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime uploadedAt = OffsetDateTime.now();
}