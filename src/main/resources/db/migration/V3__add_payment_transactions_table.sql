-- ============================================================
-- V3: Add held fields to appointment_slots & create payment_transactions table
-- ============================================================

ALTER TABLE appointment_slots
    ADD COLUMN held_at DATETIME NULL AFTER status,
    ADD COLUMN hold_expires_at DATETIME NULL AFTER held_at;

CREATE TABLE payment_transactions
(
    id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    txn_ref          VARCHAR(100)   NOT NULL,
    appointment_id   BIGINT UNSIGNED NOT NULL,
    amount           DECIMAL(12, 2) NOT NULL,
    payment_provider VARCHAR(50)    NULL,
    bank_code        VARCHAR(20)    NULL,
    transaction_no   VARCHAR(100)   NULL,
    status           VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
    response_code    VARCHAR(20)    NULL,
    payment_time     DATETIME       NULL,
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_payment_transactions_txn_ref
        UNIQUE (txn_ref),

    CONSTRAINT fk_payment_transactions_appointment
        FOREIGN KEY (appointment_id)
            REFERENCES appointments (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
