-- ============================================================
-- Migration V15: Add reminder_sent_at to appointments
-- ============================================================

ALTER TABLE appointments
    ADD COLUMN reminder_sent_at DATETIME NULL AFTER checked_in_at;
