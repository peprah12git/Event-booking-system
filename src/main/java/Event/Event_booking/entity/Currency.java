package Event.Event_booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "currencies")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {

    @Id
    @Column(name = "code", length = 3)
    private String code;

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Column(name = "symbol", nullable = false, length = 5)
    private String symbol;
}