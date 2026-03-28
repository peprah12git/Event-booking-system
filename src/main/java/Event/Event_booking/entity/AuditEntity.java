package Event.Event_booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit_entities")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;
}
