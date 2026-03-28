package Event.Event_booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_statuses")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "name", nullable = false, length = 30, unique = true)
    private String name;
}