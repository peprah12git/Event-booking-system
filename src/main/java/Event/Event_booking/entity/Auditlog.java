package Event.Event_booking.entity;

import jakarta.persistence.*;
        import lombok.*;
        import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_user",   columnList = "user_id"),
                @Index(name = "idx_audit_entity", columnList = "entity_id, record_id"),
                @Index(name = "idx_audit_ts",     columnList = "ts DESC")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditlog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nullable — system/automated actions have no associated user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false)
    private AuditAction action;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entity_id", nullable = false)
    private AuditEntity entity;

    /** Primary key of the affected row in the target table. */
    @Column(name = "record_id", nullable = false)
    private Long recordId;

    /**
     * JSONB column — Hibernate maps it as a {@code Map<String, Object>}.
     * The GIN indexes on old_values / new_values are defined in DDL.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_values", columnDefinition = "jsonb")
    private Map<String, Object> oldValues;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_values", columnDefinition = "jsonb")
    private Map<String, Object> newValues;

    /**
     * PostgreSQL INET type (IPv4 & IPv6).
     * Stored as {@link InetAddress}; Hibernate handles the conversion via
     * the {@code postgresql} dialect.
     */
    @Column(name = "ip_address", columnDefinition = "inet")
    private InetAddress ipAddress;

    /** Renamed from {@code timestamp} to avoid the SQL reserved word. */
    @Column(name = "ts", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime ts = OffsetDateTime.now();
}