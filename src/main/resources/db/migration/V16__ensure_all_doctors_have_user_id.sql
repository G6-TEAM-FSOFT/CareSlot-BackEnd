-- ============================================================
-- V16__ensure_all_doctors_have_user_id.sql
-- CareSlot Migration: Seed Users for Doctors missing user_id
-- ============================================================

-- 1. Insert missing User accounts for Doctors 3, 4, 5, 7, 8, 9, 10
INSERT INTO users (id, email, password_hash, full_name, phone, role, clinic_id, status)
VALUES
(18, 'doctor.hoan@careslot.vn', '123456', 'TS.BS Nguyễn Phúc Hoàn', '0900000018', 'DOCTOR', 1, 'ACTIVE'),
(19, 'doctor.huong@careslot.vn', '123456', 'ThS.BSNT Đỗ Thùy Hương', '0900000019', 'DOCTOR', 1, 'ACTIVE'),
(20, 'doctor.yen@careslot.vn', '123456', 'ThS.BSNT Trịnh Thị Ngọc Yến', '0900000020', 'DOCTOR', 1, 'ACTIVE'),
(21, 'doctor.quan@careslot.vn', '123456', 'ThS.BS Phạm Minh Quân', '0900000021', 'DOCTOR', 1, 'ACTIVE'),
(22, 'doctor.hoang@careslot.vn', '123456', 'ThS.BSNT Nguyễn Xuân Đức Hoàng', '0900000022', 'DOCTOR', 1, 'ACTIVE'),
(23, 'doctor.linh@careslot.vn', '123456', 'PGS.TS.BSNT Lê Tuấn Linh', '0900000023', 'DOCTOR', 1, 'ACTIVE'),
(24, 'doctor.luu@careslot.vn', '123456', 'PGS.TS.BSNT Đoàn Tiến Lưu', '0900000024', 'DOCTOR', 1, 'ACTIVE')
ON DUPLICATE KEY UPDATE 
    email = VALUES(email),
    full_name = VALUES(full_name),
    role = VALUES(role),
    clinic_id = VALUES(clinic_id),
    status = VALUES(status);

-- 2. Link user_id for remaining Doctors
UPDATE doctors SET user_id = 18 WHERE id = 3;  -- TS.BS Nguyễn Phúc Hoàn
UPDATE doctors SET user_id = 19 WHERE id = 4;  -- ThS.BSNT Đỗ Thùy Hương
UPDATE doctors SET user_id = 20 WHERE id = 5;  -- ThS.BSNT Trịnh Thị Ngọc Yến
UPDATE doctors SET user_id = 21 WHERE id = 7;  -- ThS.BS Phạm Minh Quân
UPDATE doctors SET user_id = 22 WHERE id = 8;  -- ThS.BSNT Nguyễn Xuân Đức Hoàng
UPDATE doctors SET user_id = 23 WHERE id = 9;  -- PGS.TS.BSNT Lê Tuấn Linh
UPDATE doctors SET user_id = 24 WHERE id = 10; -- PGS.TS.BSNT Đoàn Tiến Lưu
