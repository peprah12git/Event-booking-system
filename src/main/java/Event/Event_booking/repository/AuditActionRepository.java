package Event.Event_booking.repository;

import Event.Event_booking.entity.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuditActionRepository extends JpaRepository<AuditAction, Short> {
    Optional<AuditAction> findByName(String name);
}
