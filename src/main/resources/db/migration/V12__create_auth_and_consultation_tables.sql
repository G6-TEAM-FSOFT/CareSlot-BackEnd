-- V12__create_auth_and_consultation_tables.sql

CREATE TABLE IF NOT EXISTS auth_sessions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    refresh_jti_hash VARCHAR(64) NOT NULL UNIQUE,
    token_family_id VARCHAR(64) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(512),
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    last_used_at DATETIME(6),
    revoked_at DATETIME(6),
    CONSTRAINT fk_auth_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_auth_sessions_jti (refresh_jti_hash),
    INDEX idx_auth_sessions_family (token_family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS medical_consultations (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT UNSIGNED NOT NULL,
    visit_id BIGINT UNSIGNED NOT NULL,
    medical_record_number VARCHAR(50),
    hospital_admission_number VARCHAR(50),
    patient_name_snapshot VARCHAR(100) NOT NULL,
    patient_age INT NOT NULL,
    patient_gender VARCHAR(10) NOT NULL,
    treated_from_date DATE,
    treated_to_date DATE,
    bed_number VARCHAR(20),
    room_number VARCHAR(20),
    department_name VARCHAR(100),
    diagnosis_text TEXT,
    consultation_time DATETIME(6) NOT NULL,
    chairperson_name VARCHAR(100) NOT NULL,
    secretary_name VARCHAR(100) NOT NULL,
    participants_text TEXT,
    clinical_summary TEXT,
    conclusion_text TEXT,
    treatment_plan TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'FINAL',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_consultation_patient FOREIGN KEY (patient_id) REFERENCES patient_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_consultation_visit FOREIGN KEY (visit_id) REFERENCES visits(id) ON DELETE CASCADE,
    INDEX idx_consultation_patient (patient_id),
    INDEX idx_consultation_visit (visit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
