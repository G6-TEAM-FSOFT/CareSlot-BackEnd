-- ============================================================
-- Migration V4: Add profile_type and enforce at most 1 PRIMARY profile per User
-- ============================================================

-- 1. Add column profile_type
ALTER TABLE patient_profiles
    ADD COLUMN profile_type VARCHAR(20) NOT NULL DEFAULT 'FAMILY' AFTER user_id;

-- 2. Backfill existing data: if relationship = 'SELF' then profile_type = 'PRIMARY'
UPDATE patient_profiles
    SET profile_type = 'PRIMARY'
    WHERE relationship = 'SELF';

-- 3. Add Virtual Generated Column for Primary User ID:
-- When profile_type = 'PRIMARY', primary_user_id = user_id.
-- When profile_type = 'FAMILY', primary_user_id = NULL.
ALTER TABLE patient_profiles
    ADD COLUMN primary_user_id BIGINT UNSIGNED
    GENERATED ALWAYS AS (CASE WHEN profile_type = 'PRIMARY' THEN user_id ELSE NULL END) VIRTUAL;

-- 4. Enforce at most 1 PRIMARY profile per user via UNIQUE constraint on primary_user_id.
-- Since NULL values are not considered duplicate in MySQL UNIQUE indexes,
-- users can have unlimited FAMILY profiles (primary_user_id IS NULL)
-- but at most 1 PRIMARY profile (primary_user_id = user_id).
ALTER TABLE patient_profiles
    ADD CONSTRAINT uq_patient_profiles_user_primary
    UNIQUE (primary_user_id);

-- 5. Add composite index for efficient queries by user_id, profile_type, and status
CREATE INDEX idx_patient_profiles_user_type_status
    ON patient_profiles (user_id, profile_type, status);
