-- ============================================================
-- Migration V6: Add extra demographic & identity fields to patient_profiles
-- ============================================================

ALTER TABLE patient_profiles
    ADD COLUMN identity_card   VARCHAR(50)  NULL AFTER phone,
    ADD COLUMN card_issue_date DATE         NULL AFTER identity_card,
    ADD COLUMN ethnicity       VARCHAR(50)  NOT NULL DEFAULT 'Kinh' AFTER card_issue_date,
    ADD COLUMN nationality     VARCHAR(50)  NOT NULL DEFAULT 'Việt Nam' AFTER ethnicity,
    ADD COLUMN occupation      VARCHAR(100) NULL AFTER nationality,
    ADD COLUMN address         VARCHAR(500) NULL AFTER occupation;
