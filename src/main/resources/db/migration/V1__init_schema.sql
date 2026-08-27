-- ============================================================
-- CareSlot Database Schema
-- MySQL 8+
-- ============================================================


-- ============================================================
-- 1. CLINICS
-- ============================================================

CREATE TABLE clinics
(
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    name        VARCHAR(255) NOT NULL,
    address     VARCHAR(500) NOT NULL,

    latitude    DECIMAL(10, 7) NULL,
    longitude   DECIMAL(10, 7) NULL,

    phone       VARCHAR(20) NULL,
    description TEXT NULL,

    status      VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',

    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 2. SPECIALTIES
-- ============================================================

CREATE TABLE specialties
(
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    name        VARCHAR(150) NOT NULL,
    description TEXT NULL,

    status      VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',

    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 2.1 CLINIC_SPECIALTIES (Many-to-Many Join Table)
-- ============================================================

CREATE TABLE clinic_specialties
(
    clinic_id    BIGINT UNSIGNED NOT NULL,
    specialty_id BIGINT UNSIGNED NOT NULL,

    PRIMARY KEY (clinic_id, specialty_id),

    CONSTRAINT fk_clinic_specialties_clinic
        FOREIGN KEY (clinic_id)
            REFERENCES clinics (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,

    CONSTRAINT fk_clinic_specialties_specialty
        FOREIGN KEY (specialty_id)
            REFERENCES specialties (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;



-- ============================================================
-- 3. USERS
-- ============================================================

CREATE TABLE users
(
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    full_name     VARCHAR(255) NOT NULL,
    phone         VARCHAR(20) NULL,

    role          VARCHAR(30)  NOT NULL,

    /*
        NULL:
        - PATIENT
        - ADMIN

        Có giá trị:
        - CLINIC_PARTNER
        - CLINIC_STAFF
    */
    clinic_id     BIGINT UNSIGNED NULL,

    status        VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',

    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_email
        UNIQUE (email),

    CONSTRAINT fk_users_clinic
        FOREIGN KEY (clinic_id)
            REFERENCES clinics (id)
            ON UPDATE CASCADE
            ON DELETE SET NULL

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 4. PATIENT PROFILES
-- User 1 ---- N PatientProfile
-- ============================================================

CREATE TABLE patient_profiles
(
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    user_id       BIGINT UNSIGNED NOT NULL,

    full_name     VARCHAR(255) NOT NULL,
    date_of_birth DATE NULL,

    gender        VARCHAR(20) NULL,
    phone         VARCHAR(20) NULL,

    /*
        Ví dụ:
        SELF
        FATHER
        MOTHER
        SPOUSE
        CHILD
        OTHER
    */
    relationship  VARCHAR(30)  NOT NULL DEFAULT 'SELF',

    status        VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',

    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_patient_profiles_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 5. DOCTORS
--
-- Clinic    1 ---- N Doctor
-- Specialty 1 ---- N Doctor
-- ============================================================

CREATE TABLE doctors
(
    id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    clinic_id        BIGINT UNSIGNED NOT NULL,
    specialty_id     BIGINT UNSIGNED NOT NULL,

    full_name        VARCHAR(255)   NOT NULL,

    /*
        Ví dụ:
        BS
        ThS.BS
        TS.BS
        PGS.TS.BS
    */
    title            VARCHAR(100) NULL,

    bio              TEXT NULL,
    avatar_url       VARCHAR(1000) NULL,

    consultation_fee DECIMAL(12, 2) NOT NULL DEFAULT 0,

    status           VARCHAR(30)    NOT NULL DEFAULT 'ACTIVE',

    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_doctors_clinic
        FOREIGN KEY (clinic_id)
            REFERENCES clinics (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,

    CONSTRAINT fk_doctors_specialty
        FOREIGN KEY (specialty_id)
            REFERENCES specialties (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 6. APPOINTMENT SLOTS
--
-- Doctor 1 ---- N AppointmentSlot
-- ============================================================

CREATE TABLE appointment_slots
(
    id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    doctor_id        BIGINT UNSIGNED NOT NULL,

    appointment_date DATE        NOT NULL,

    start_time       TIME        NOT NULL,
    end_time         TIME        NOT NULL,

    room_name        VARCHAR(100) NULL,

    /*
        Gợi ý status:

        AVAILABLE
        HELD
        BOOKED
        BLOCKED
        CANCELLED
    */
    status           VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',

    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_appointment_slots_doctor
        FOREIGN KEY (doctor_id)
            REFERENCES doctors (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,

    /*
        Không cho tạo 2 record slot giống hệt nhau
        cho cùng bác sĩ.
    */
    CONSTRAINT uq_doctor_appointment_slot
        UNIQUE (
                doctor_id,
                appointment_date,
                start_time,
                end_time
            )

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 7. APPOINTMENTS
--
-- PatientProfile  1 ---- N Appointment
-- AppointmentSlot 1 ---- 0..N Appointment
-- ============================================================

CREATE TABLE appointments
(
    id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    booking_code       VARCHAR(50)    NOT NULL,

    patient_profile_id BIGINT UNSIGNED NOT NULL,
    slot_id            BIGINT UNSIGNED NOT NULL,

    symptom_note       TEXT NULL,

    /*
        Snapshot giá tại thời điểm booking.
        Việc thay đổi giá của Doctor sau này
        không làm thay đổi booking cũ.
    */
    consultation_fee   DECIMAL(12, 2) NOT NULL DEFAULT 0,

    deposit_amount     DECIMAL(12, 2) NOT NULL DEFAULT 0,

    /*
        Có thể sử dụng theo workflow thực tế:

        PENDING_PAYMENT
        PENDING_APPROVAL
        CONFIRMED
        REJECTED
        CANCELLED
        CHECKED_IN
        EXPIRED
    */
    status             VARCHAR(30)    NOT NULL DEFAULT 'PENDING_PAYMENT',

    created_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    approved_at        DATETIME NULL,
    approved_by        BIGINT UNSIGNED NULL,

    rejected_at        DATETIME NULL,
    cancelled_at       DATETIME NULL,
    checked_in_at      DATETIME NULL,

    updated_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_appointments_booking_code
        UNIQUE (booking_code),

    CONSTRAINT fk_appointments_patient_profile
        FOREIGN KEY (patient_profile_id)
            REFERENCES patient_profiles (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,

    CONSTRAINT fk_appointments_slot
        FOREIGN KEY (slot_id)
            REFERENCES appointment_slots (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,

    CONSTRAINT fk_appointments_approved_by
        FOREIGN KEY (approved_by)
            REFERENCES users (id)
            ON UPDATE CASCADE
            ON DELETE SET NULL

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;