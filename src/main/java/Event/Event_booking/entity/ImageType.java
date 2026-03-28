package Event.Event_booking.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "image_types")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "name", nullable = false, length = 20, unique = true)
    private String name;
}