# AGENTS.md - Event Booking System AI Guide

## Project Overview
**Spring Boot 4.0.5 event booking platform** with JWT authentication, PostgreSQL persistence, Redis token management, and audit logging. Uses **Java 21** with Maven, Lombok, and Jakarta/Spring Security.

## Architecture Essentials

### Core Components
- **Controllers** (`controller/`): RESTful endpoints under `/api/auth/**` - registration, login, token refresh
- **Services** (`service/`): Business logic; currently `UserService` handles auth workflows
- **Entities** (`entity/`): JPA models with lazy loading and audit trails (16 entity types)
- **Security**: JWT-based stateless auth with `JwtAuthenticationFilter` → `SecurityConfig`
- **Repositories**: Spring Data JPA; supports `User`, `Role`, `AuditLog`, etc.
- **DTOs** (`dto/`): Input validation via Jakarta Bean Validation with strict patterns (see `UserRegistrationDTO`)

### Data Flow
1. Request arrives → `JwtAuthenticationFilter` extracts Bearer token from `Authorization` header
2. `JwtUtil` validates token (issuer, expiration, signature)
3. `UserDetailsService` loads user details by email
4. `SecurityContext` populated with `UsernamePasswordAuthenticationToken`
5. Route handler executes with authentication context

## Key Patterns & Conventions

### JWT Token Management
- **Generation**: `JwtUtil.generateToken(email, role)` creates HS256 signed tokens with email as subject and role as claim
- **Expiration**: Access tokens = 1h (3600000ms), refresh tokens = 7d (604800000ms, set as 2x expiration in code)
- **Extraction**: `JwtUtil.extractEmail()` and `JwtUtil.extractRole()` parse claims
- **Validation**: `JwtUtil.isTokenValid()` catches `JwtException` to verify integrity
- **Token Refresh**: `/api/auth/refresh` endpoint regenerates access token from valid refresh token

### Entity Design Patterns
- **Inheritance structure**: All entities use `@Builder`, `@Lombok`, and `@GeneratedValue(IDENTITY)`
- **Timestamps**: `OffsetDateTime` with `@CreationTimestamp` for immutable creation times; `@PreUpdate` callbacks sync Java state with DB
- **Lazy Loading**: All relationships use `FetchType.LAZY` to prevent N+1 queries
- **Indexing**: Strategic indexes on foreign keys (`organizer_id`, `user_id`) and temporal columns (`event_date`, `ts`)
- **Audit Trail**: `Auditlog` entity captures user actions with JSONB old/new values, IP addresses (PostgreSQL INET type), and action metadata

### Validation Strategy
- DTOs enforce validation via `@NotBlank`, `@Email`, `@Pattern` annotations
- Password regex enforces: minimum 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char (`@$!%*?&`)
- Field-level errors returned as `Map<String, String>` via `GlobalExceptionHandler`

### Exception Handling
- `@RestControllerAdvice` catches `RuntimeException` (business logic errors) and `MethodArgumentNotValidException` (validation failures)
- Returns HTTP 400 with error details in JSON
- Pattern: throw `RuntimeException("Descriptive message")` in services

## Build & Deployment

### Maven Commands
```bash
./mvnw clean compile        # Compile and validate Java 21 syntax
./mvnw clean package        # Build JAR with dependencies
./mvnw spring-boot:run      # Start embedded Tomcat on port 8080
./mvnw test                 # Run test suite (minimal coverage currently)
```

### Database Setup
- **Type**: PostgreSQL (via `spring-boot-starter-data-jpa`)
- **Connection**: `jdbc:postgresql://localhost:5432/event_management_system`
- **Credentials**: User `postgres`, password via `${DB_PASSWORD}` env var
- **DDL Strategy**: `ddl-auto=none` (schema is pre-created; use migrations for schema changes)
- **PostgreSQL-Specific**: JSONB columns in audit logs, INET type for IP addresses, triggers for `updated_at`

### Security Configuration
- **CSRF**: Disabled (stateless API)
- **Sessions**: `STATELESS` policy (no server-side session storage)
- **Permit All**: `/api/auth/**` routes (registration & login public)
- **Auth Required**: All other routes checked by `JwtAuthenticationFilter`
- **Password Encoding**: BCrypt with Spring default cost factor

## Critical Files for Context

| File | Purpose |
|------|---------|
| `SecurityConfig.java` | JWT filter chain setup, password encoder bean, CORS/CSRF policy |
| `JwtUtil.java` | Token generation, validation, claim extraction using JJWT 0.11.5 |
| `JwtAuthenticationFilter.java` | Request interceptor; extracts token and populates `SecurityContext` |
| `UserService.java` | User registration (role assignment, duplicate check), login (password match) |
| `GlobalExceptionHandler.java` | Centralized error responses for runtime exceptions and validation errors |
| `User.java` | Core user entity; email uniqueness enforced, organizer approval flag |
| `Auditlog.java` | Immutable audit records with JSONB columns, lazy-loaded relationships, indexes |
| `Event.java` | Event details with capacity tracking, pricing (BigDecimal), category/location refs |
| `Booking.java` | Booking records linked to user+event, status enum, QR code support, soft-delete via `cancelledAt` |

## Common Development Tasks

### Adding a New Endpoint
1. Create Controller in `controller/`
2. Inject required services via constructor (Lombok `@AllArgsConstructor`)
3. Use `@PostMapping` or `@GetMapping` with `/api/resource/**` path
4. Create request DTO in `dto/` with validation annotations
5. Service handles business logic; throw `RuntimeException` for errors
6. Return `ResponseEntity.ok(data)` or `.status(HttpStatus.CREATED)`

### Adding a New Entity
1. Create class in `entity/` with `@Entity`, `@Table`, Lombok annotations
2. Use `@ManyToOne(fetch = FetchType.LAZY)` for relationships
3. Add indexes via `@Table(indexes = {...})` for query performance
4. Create repository interface extending `JpaRepository<Entity, Long>`
5. Migrations handled externally (DDL auto is disabled)

### Testing Authentication
- POST `/api/auth/register` with `UserRegistrationDTO` → returns access + refresh tokens
- POST `/api/auth/login` with `UserLoginDTO` → returns tokens
- POST `/api/auth/refresh` with `{"refreshToken": "..."}` → returns new access token
- Use `Authorization: Bearer <token>` header for protected routes

## Dependencies Overview

| Dependency | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 4.0.5 | Framework foundation |
| Spring Security | (Boot parent) | Authentication & authorization |
| Spring Data JPA | (Boot parent) | ORM & repository layer |
| PostgreSQL Driver | (Boot parent) | DB connectivity |
| JJWT | 0.11.5 | JWT creation & validation (HMAC-SHA256) |
| Spring Data Redis | (Boot parent) | Token blacklist potential (configured but unused) |
| Lombok | (Boot parent) | Annotation-driven code generation (@Getter, @Setter, @Builder) |
| Jakarta Validation | 4.0.0-M1 | Bean validation API (ConstraintValidator implementations) |

## Notes for AI Agents

- **Package naming**: Uses `Event.Event_booking` (not hyphens) due to Maven build requirement
- **PasswordHash field**: Misnomer; actually stores BCrypt-encoded password, not plaintext hash
- **Redis integration**: Present but not yet leveraged for token blacklist
- **Audit logging**: Comprehensive structure but no UI/API endpoint to retrieve audit history yet
- **Entity setters**: Lombok generates all setters; use `@Builder` pattern for complex object creation
- **No custom UserDetailsService**: Falls back to Spring Security defaults; email used as username

