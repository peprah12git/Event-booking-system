package Event.Event_booking.repository;

import Event.Event_booking.entity.Auditlog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<Auditlog, Long> {
}
