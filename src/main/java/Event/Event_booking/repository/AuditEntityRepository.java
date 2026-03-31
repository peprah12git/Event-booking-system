package Event.Event_booking.repository;

import Event.Event_booking.entity.AuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuditEntityRepository extends JpaRepository<AuditEntity, Long> {
    Optional<AuditEntity> findByName(String name);
}
