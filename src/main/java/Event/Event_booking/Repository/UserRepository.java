package Event.Event_booking.Repository;

import Event.Event_booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // check if email already exists during registration
    boolean existsByEmail(String email);


}
