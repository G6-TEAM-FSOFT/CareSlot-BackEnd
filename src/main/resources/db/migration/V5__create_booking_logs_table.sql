-- ============================================================
-- V5__create_booking_logs_table.sql
-- CareSlot Booking Lifecycle Audit Logs
-- ============================================================

CREATE TABLE booking_logs (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    appointment_id  BIGINT UNSIGNED NOT NULL,
    booking_code    VARCHAR(50) NOT NULL,
    previous_status VARCHAR(30) NULL,
    new_status      VARCHAR(30) NOT NULL,
    event_type      VARCHAR(50) NOT NULL,
    note            TEXT NULL,
    actor           VARCHAR(100) NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_booking_logs_appointment
        FOREIGN KEY (appointment_id)
            REFERENCES appointments (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
