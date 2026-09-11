-- Persistent request deduplication and allocation rotation. Existing appointments keep NULL keys.
ALTER TABLE appointments
    ADD COLUMN booking_user_id BIGINT UNSIGNED NULL,
    ADD COLUMN request_key VARCHAR(80) NULL,
    ADD CONSTRAINT fk_appointment_booking_user FOREIGN KEY (booking_user_id) REFERENCES users(id),
    ADD UNIQUE KEY uk_appointment_booking_request (booking_user_id, request_key),
    ADD INDEX idx_appointment_slot_status (slot_id, status);

CREATE TABLE booking_allocation_cursors (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    clinic_id BIGINT UNSIGNED NOT NULL,
    specialty_id BIGINT UNSIGNED NOT NULL,
    last_doctor_id BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_allocation_clinic_specialty (clinic_id, specialty_id),
    CONSTRAINT fk_allocation_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_allocation_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_slot_room_date_time ON appointment_slots (room_id, appointment_date, start_time, end_time);
CREATE INDEX idx_slot_booking_status_date ON appointment_slots (status, appointment_date, start_time);
CREATE INDEX idx_payment_appointment_status ON payment_transactions (appointment_id, status, created_at);

-- Deliberately fails for legacy duplicate visits: reconcile duplicates before deploying this migration.
ALTER TABLE visits ADD CONSTRAINT uk_visit_appointment UNIQUE (appointment_id);

-- Only normalize legacy room names when a single room in the doctor's clinic matches exactly.
-- Ambiguous or unmapped slots remain unavailable to automatic booking until staff assigns a room.
UPDATE appointment_slots s
JOIN doctors d ON d.id = s.doctor_id
JOIN (
    SELECT clinic_id, name, MIN(id) AS room_id FROM rooms
    WHERE status = 'ACTIVE' AND room_type = 'CONSULTATION'
    GROUP BY clinic_id, name HAVING COUNT(*) = 1
) matching_room ON matching_room.clinic_id = d.clinic_id AND matching_room.name = s.room_name
SET s.room_id = matching_room.room_id
WHERE s.room_id IS NULL;

UPDATE appointment_slots s
JOIN doctors d ON d.id = s.doctor_id
JOIN (
    SELECT clinic_id, room_number, MIN(id) AS room_id FROM rooms
    WHERE status = 'ACTIVE' AND room_type = 'CONSULTATION'
    GROUP BY clinic_id, room_number HAVING COUNT(*) = 1
) matching_room ON matching_room.clinic_id = d.clinic_id AND matching_room.room_number = s.room_name
SET s.room_id = matching_room.room_id
WHERE s.room_id IS NULL;