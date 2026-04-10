package Event.Event_booking.repository;

import Event.Event_booking.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // check if email already exists during registration
    boolean existsByEmail(String email);

    // US-007: users who requested organizer access but are not yet approved
    Page<User> findByRole_NameAndIsOrganizerApprovedFalse(String roleName, Pageable pageable);
}
