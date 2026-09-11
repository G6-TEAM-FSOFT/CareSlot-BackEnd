-- ============================================================
-- CareSlot Migration V10: Seed Complete Real Flow Test Data
-- Real_Flow.md Reference Benchmark Workflow Data
-- ============================================================

-- 1. Thêm Chuyên khoa Tiêu Hóa (Specialty ID = 4)
INSERT INTO specialties (id, name, description, status) 
VALUES (4, 'Tiêu Hóa', 'Chuyên khoa Tiêu Hóa & Gan Mật Tụy', 'ACTIVE')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 2. Đăng ký Chuyên khoa Tiêu Hóa cho Cơ sở Tôn Thất Tùng (Clinic ID = 1)
INSERT INTO clinic_specialties (clinic_id, specialty_id) 
VALUES (1, 4)
ON DUPLICATE KEY UPDATE clinic_id = VALUES(clinic_id);

-- 3. Cập nhật / Seed Bác sĩ chuyên khoa Tiêu hóa BS. Trần Hoàng Minh (Doctor ID = 1)
INSERT INTO doctors (id, clinic_id, specialty_id, full_name, title, bio, avatar_url, consultation_fee, status)
VALUES (1, 1, 4, 'BS. Trần Hoàng Minh', 'PGS.TS.BS', 'Trưởng khoa Tiêu hóa Bệnh viện Đại học Y Hà Nội', NULL, 500000.00, 'ACTIVE')
ON DUPLICATE KEY UPDATE 
    full_name = 'BS. Trần Hoàng Minh', 
    title = 'PGS.TS.BS', 
    specialty_id = 4, 
    consultation_fee = 500000.00;

-- 4. Thêm Users mới cho Đợt test Real Flow (BS Minh & Bệnh nhân Nguyễn Minh Anh)
INSERT INTO users (id, email, password_hash, full_name, phone, role, clinic_id, status)
VALUES
(14, 'doctor.minh@careslot.vn', '123456', 'BS. Trần Hoàng Minh', '0900000014', 'DOCTOR', 1, 'ACTIVE'),
(15, 'nguyenminhanh@example.com', '123456', 'Nguyễn Minh Anh', '0901234567', 'PATIENT', NULL, 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- 5. Thêm Patient Profile cho Bệnh nhân Nguyễn Minh Anh (Profile ID = 10)
INSERT INTO patient_profiles (id, user_id, full_name, date_of_birth, gender, phone, relationship, status)
VALUES (10, 15, 'Nguyễn Minh Anh', '1988-06-15', 'MALE', '0901234567', 'SELF', 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- 6. Tạo các Slot khám ngày 09/09/2026 cho BS. Trần Hoàng Minh tại Phòng 305 (Room ID = 1)
INSERT INTO appointment_slots (id, doctor_id, appointment_date, start_time, end_time, room_name, status, room_id)
VALUES
(20, 1, '2026-09-11', '09:00:00', '09:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(21, 1, '2026-09-11', '09:20:00', '09:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(22, 1, '2026-09-11', '09:40:00', '10:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(23, 1, '2026-09-11', '10:00:00', '10:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(24, 1, '2026-09-11', '10:20:00', '10:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(25, 1, '2026-09-11', '10:20:00', '10:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(26, 1, '2026-09-11', '17:00:00', '17:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(27, 1, '2026-09-11', '17:20:00', '17:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(28, 1, '2026-09-11', '17:40:00', '18:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(29, 1, '2026-09-12', '09:00:00', '09:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(30, 1, '2026-09-12', '09:20:00', '09:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(31, 1, '2026-09-12', '09:40:00', '10:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(32, 1, '2026-09-12', '10:00:00', '10:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(33, 1, '2026-09-12', '10:20:00', '10:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(34, 1, '2026-09-12', '10:20:00', '10:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(35, 1, '2026-09-12', '17:00:00', '17:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(36, 1, '2026-09-12', '17:20:00', '17:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(37, 1, '2026-09-12', '17:40:00', '18:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(38, 1, '2026-09-13', '09:00:00', '09:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(39, 1, '2026-09-13', '09:20:00', '09:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(40, 1, '2026-09-13', '09:40:00', '10:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(41, 1, '2026-09-13', '10:00:00', '10:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(42, 1, '2026-09-13', '10:20:00', '10:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(43, 1, '2026-09-13', '10:20:00', '10:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(44, 1, '2026-09-13', '17:00:00', '17:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(45, 1, '2026-09-13', '17:20:00', '17:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(46, 1, '2026-09-13', '17:40:00', '18:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1)
ON DUPLICATE KEY UPDATE status = VALUES(status), room_id = VALUES(room_id);
