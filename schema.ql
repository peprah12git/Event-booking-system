CREATE TABLE roles (
    id    SMALLSERIAL  NOT NULL,
    name  VARCHAR(50)  NOT NULL,   -- e.g. ATTENDEE, ORGANIZER, ADMIN
    PRIMARY KEY (id),
    CONSTRAINT uq_role_name UNIQUE (name)
);

-- Currencies are reused across events and payments → own table (3NF)
CREATE TABLE currencies (
    code    CHAR(3)      NOT NULL,  -- ISO-4217: USD, GHS, EUR …
    name    VARCHAR(60)  NOT NULL,
    symbol  VARCHAR(5)   NOT NULL,
    PRIMARY KEY (code)
);

-- Event categories → own table (3NF, avoids repeating strings)
CREATE TABLE categories (
    id    SMALLSERIAL   NOT NULL,
    name  VARCHAR(100)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_category_name UNIQUE (name)
);

-- Booking statuses (PENDING, CONFIRMED, CANCELLED …) → lookup (3NF)
CREATE TABLE booking_statuses (
    id    SMALLSERIAL  NOT NULL,
    name  VARCHAR(30)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_bs_name UNIQUE (name)
);

-- Payment statuses (PENDING, COMPLETED, FAILED, REFUNDED …) → lookup (3NF)
CREATE TABLE payment_statuses (
    id    SMALLSERIAL  NOT NULL,
    name  VARCHAR(30)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_ps_name UNIQUE (name)
);

-- Payment gateways (STRIPE, PAYSTACK, FLUTTERWAVE …) → lookup (3NF)
CREATE TABLE payment_gateways (
    id    SMALLSERIAL  NOT NULL,
    name  VARCHAR(50)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_pg_name UNIQUE (name)
);

-- Image types (BANNER, THUMBNAIL, GALLERY) → lookup (3NF)
CREATE TABLE image_types (
    id    SMALLSERIAL  NOT NULL,
    name  VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_it_name UNIQUE (name)
);

-- Audit actions (CREATE, UPDATE, DELETE …) → lookup (3NF)
CREATE TABLE audit_actions (
    id    SMALLSERIAL  NOT NULL,
    name  VARCHAR(50)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_aa_name UNIQUE (name)
);

-- Auditable entity types (User, Event, Booking …) → lookup (3NF)
CREATE TABLE audit_entities (
    id    SMALLSERIAL  NOT NULL,
    name  VARCHAR(50)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_ae_name UNIQUE (name)
);


-- ─────────────────────────────────────────────────────────────
--  CORE TABLES
-- ─────────────────────────────────────────────────────────────

-- 1NF  : atomic columns, no repeating groups, PK defined
-- 2NF  : all non-key columns depend on the WHOLE PK (single-col PK → trivially satisfied)
-- 3NF  : no transitive dependencies; role_id → roles table, not stored as a plain string

-- PostgreSQL: BIGSERIAL = auto-incrementing BIGINT
--             TIMESTAMPTZ stores timestamp with time zone (best practice in PG)
--             No UNSIGNED — PostgreSQL integers are always signed; use CHECK instead

CREATE TABLE users (
    id                    BIGSERIAL     NOT NULL,
    name                  VARCHAR(120)  NOT NULL,
    email                 VARCHAR(254)  NOT NULL,
    password_hash         CHAR(60)      NOT NULL,  -- bcrypt fixed length
    role_id               SMALLINT      NOT NULL,
    is_organizer_approved BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    CONSTRAINT uq_user_email UNIQUE (email),
    CONSTRAINT fk_user_role  FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- Location decomposed into atomic columns (1NF: no multi-valued location string)
-- Separate table achieves full 3NF / BCNF for location data
CREATE TABLE locations (
    id           SERIAL        NOT NULL,
    venue_name   VARCHAR(150),
    address_line VARCHAR(200),
    city         VARCHAR(100),
    state        VARCHAR(100),
    country      VARCHAR(100),
    postal_code  VARCHAR(20),
    -- PostgreSQL has a native POINT type, but NUMERIC keeps it portable
    latitude     NUMERIC(9,6),
    longitude    NUMERIC(9,6),
    PRIMARY KEY (id)
);

CREATE TABLE events (
    id                BIGSERIAL      NOT NULL,
    organizer_id      BIGINT         NOT NULL,
    title             VARCHAR(200)   NOT NULL,
    description       TEXT,
    event_date        TIMESTAMPTZ    NOT NULL,
    location_id       INT,                        -- FK to locations (3NF), nullable
    category_id       SMALLINT       NOT NULL,    -- FK to categories (3NF)
    capacity          INT            NOT NULL,
    available_tickets INT            NOT NULL,
    price             NUMERIC(10,2)  NOT NULL DEFAULT 0.00,
    currency_code     CHAR(3)        NOT NULL,    -- FK to currencies (3NF)
    primary_image_url VARCHAR(500),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    -- PostgreSQL uses a trigger for auto-update; see trigger below
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    CONSTRAINT fk_event_organizer FOREIGN KEY (organizer_id)  REFERENCES users      (id),
    CONSTRAINT fk_event_location  FOREIGN KEY (location_id)   REFERENCES locations  (id),
    CONSTRAINT fk_event_category  FOREIGN KEY (category_id)   REFERENCES categories (id),
    CONSTRAINT fk_event_currency  FOREIGN KEY (currency_code) REFERENCES currencies (code),
    CONSTRAINT chk_capacity       CHECK (capacity >= 0),
    CONSTRAINT chk_tickets        CHECK (available_tickets >= 0
                                     AND available_tickets <= capacity)
);

-- Trigger function: keep updated_at current on every UPDATE
-- (PostgreSQL has no ON UPDATE CURRENT_TIMESTAMP column option like MySQL)
CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_events_updated_at
BEFORE UPDATE ON events
FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();


-- EventImage
-- 1NF  : each row = one image; type is an FK (atomic)
-- 2NF  : every column depends on the full PK (id)
-- 3NF  : image_type_id → image_types; uploaded_by → users; event_id → events
CREATE TABLE event_images (
    id              BIGSERIAL    NOT NULL,
    event_id        BIGINT       NOT NULL,
    uploaded_by     BIGINT       NOT NULL,
    url             VARCHAR(500) NOT NULL,
    image_type_id   SMALLINT     NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    file_size_bytes INT          NOT NULL,
    mime_type       VARCHAR(100) NOT NULL,
    display_order   SMALLINT     NOT NULL DEFAULT 0,
    uploaded_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    CONSTRAINT chk_file_size     CHECK (file_size_bytes > 0),
    CONSTRAINT fk_ei_event       FOREIGN KEY (event_id)      REFERENCES events      (id) ON DELETE CASCADE,
    CONSTRAINT fk_ei_uploader    FOREIGN KEY (uploaded_by)   REFERENCES users       (id),
    CONSTRAINT fk_ei_image_type  FOREIGN KEY (image_type_id) REFERENCES image_types (id)
);

-- Booking
-- 2NF  : qr_code depends only on the booking id
-- 3NF  : status_id → booking_statuses; no transitive deps
CREATE TABLE bookings (
    id           BIGSERIAL    NOT NULL,
    user_id      BIGINT       NOT NULL,
    event_id     BIGINT       NOT NULL,
    quantity     SMALLINT     NOT NULL DEFAULT 1,
    status_id    SMALLINT     NOT NULL,
    qr_code      VARCHAR(255),
    cancelled_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    CONSTRAINT uq_booking_qr_code  UNIQUE (qr_code),
    CONSTRAINT chk_quantity        CHECK (quantity >= 1),
    CONSTRAINT fk_booking_user     FOREIGN KEY (user_id)   REFERENCES users            (id),
    CONSTRAINT fk_booking_event    FOREIGN KEY (event_id)  REFERENCES events           (id),
    CONSTRAINT fk_booking_status   FOREIGN KEY (status_id) REFERENCES booking_statuses (id)
);

-- Payment
-- 3NF  : gateway_id → payment_gateways; status_id → payment_statuses
--        currency_code → currencies; booking_id → bookings
CREATE TABLE payments (
    id              BIGSERIAL     NOT NULL,
    booking_id      BIGINT        NOT NULL,
    amount          NUMERIC(10,2) NOT NULL,
    currency_code   CHAR(3)       NOT NULL,
    status_id       SMALLINT      NOT NULL,
    gateway_id      SMALLINT      NOT NULL,
    transaction_ref VARCHAR(200),
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    CONSTRAINT uq_transaction_ref    UNIQUE (transaction_ref),
    CONSTRAINT chk_amount            CHECK (amount >= 0),
    CONSTRAINT fk_payment_booking    FOREIGN KEY (booking_id)    REFERENCES bookings         (id),
    CONSTRAINT fk_payment_currency   FOREIGN KEY (currency_code) REFERENCES currencies       (code),
    CONSTRAINT fk_payment_status     FOREIGN KEY (status_id)     REFERENCES payment_statuses (id),
    CONSTRAINT fk_payment_gateway    FOREIGN KEY (gateway_id)    REFERENCES payment_gateways (id)
);

-- Waitlist
-- 3NF  : user_id and event_id are FKs; notified is a simple boolean
CREATE TABLE waitlist (
    id        BIGSERIAL   NOT NULL,
    user_id   BIGINT      NOT NULL,
    event_id  BIGINT      NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    notified  BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT uq_waitlist_user_event UNIQUE (user_id, event_id),
    CONSTRAINT fk_wl_user  FOREIGN KEY (user_id)  REFERENCES users  (id),
    CONSTRAINT fk_wl_event FOREIGN KEY (event_id) REFERENCES events (id)
);

-- AuditLog
-- 3NF  : action_id → audit_actions; entity_id → audit_entities; user_id → users
--        old_values / new_values stored as JSONB (binary JSON — indexed, efficient in PG)
CREATE TABLE audit_logs (
    id         BIGSERIAL   NOT NULL,
    user_id    BIGINT,                  -- NULL if system/automated action
    action_id  SMALLINT    NOT NULL,
    entity_id  SMALLINT    NOT NULL,
    record_id  BIGINT      NOT NULL,   -- PK of the affected row
    old_values JSONB,
    new_values JSONB,
    ip_address INET,                   -- PostgreSQL native INET type (IPv4 & IPv6)
    ts         TIMESTAMPTZ NOT NULL DEFAULT NOW(),  -- 'timestamp' is reserved in PG
    PRIMARY KEY (id),
    CONSTRAINT fk_al_user   FOREIGN KEY (user_id)   REFERENCES users          (id) ON DELETE SET NULL,
    CONSTRAINT fk_al_action FOREIGN KEY (action_id) REFERENCES audit_actions  (id),
    CONSTRAINT fk_al_entity FOREIGN KEY (entity_id) REFERENCES audit_entities (id)
);


-- ─────────────────────────────────────────────────────────────
--  INDEXES FOR COMMON QUERY PATTERNS
-- ─────────────────────────────────────────────────────────────

-- Events: filter by organizer, date, category
CREATE INDEX idx_events_organizer ON events (organizer_id);
CREATE INDEX idx_events_date      ON events (event_date);
CREATE INDEX idx_events_category  ON events (category_id);

-- Bookings: look up by user or event
CREATE INDEX idx_bookings_user  ON bookings (user_id);
CREATE INDEX idx_bookings_event ON bookings (event_id);

-- Payments: look up by booking
CREATE INDEX idx_payments_booking ON payments (booking_id);

-- EventImages: list images by event ordered by display_order
CREATE INDEX idx_ei_event_order ON event_images (event_id, display_order);

-- AuditLog: search by user, entity, and time
CREATE INDEX idx_audit_user      ON audit_logs (user_id);
CREATE INDEX idx_audit_entity    ON audit_logs (entity_id, record_id);
CREATE INDEX idx_audit_ts        ON audit_logs (ts DESC);

-- JSONB GIN index for querying inside old_values / new_values
CREATE INDEX idx_audit_old_values ON audit_logs USING GIN (old_values);
CREATE INDEX idx_audit_new_values ON audit_logs USING GIN (new_values);


-- ─────────────────────────────────────────────────────────────
--  SEED LOOKUP DATA
-- ─────────────────────────────────────────────────────────────

INSERT INTO roles (name) VALUES ('ATTENDEE'), ('ORGANIZER'), ('ADMIN');

INSERT INTO currencies (code, name, symbol) VALUES
    ('USD', 'US Dollar',      '$'),
    ('GHS', 'Ghanaian Cedi',  '₵'),
    ('EUR', 'Euro',           '€'),
    ('GBP', 'British Pound',  '£');

INSERT INTO booking_statuses (name) VALUES
    ('PENDING'), ('CONFIRMED'), ('CANCELLED'), ('ATTENDED');

INSERT INTO payment_statuses (name) VALUES
    ('PENDING'), ('COMPLETED'), ('FAILED'), ('REFUNDED');

INSERT INTO payment_gateways (name) VALUES
    ('PAYSTACK'), ('FLUTTERWAVE'), ('STRIPE'), ('MANUAL');

INSERT INTO image_types (name) VALUES
    ('BANNER'), ('THUMBNAIL'), ('GALLERY');

INSERT INTO audit_actions (name) VALUES
    ('CREATE'), ('UPDATE'), ('DELETE'), ('LOGIN'), ('LOGOUT');

INSERT INTO audit_entities (name) VALUES
    ('User'), ('Event'), ('Booking'), ('Payment'), ('Waitlist'), ('EventImage');
