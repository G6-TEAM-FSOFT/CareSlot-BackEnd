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

-- 4. Thêm Bác sĩ thứ 2 chuyên khoa Tiêu Hóa: BS. Lê Thị Phương Thảo (Doctor ID = 11)
INSERT INTO doctors (id, clinic_id, specialty_id, full_name, title, bio, avatar_url, consultation_fee, status)
VALUES (11, 1, 4, 'BS. Lê Thị Phương Thảo', 'ThS.BS', 'Bác sĩ chuyên khoa Tiêu hóa Bệnh viện Đại học Y Hà Nội', NULL, 450000.00, 'ACTIVE')
ON DUPLICATE KEY UPDATE 
    full_name = VALUES(full_name),
    title = VALUES(title),
    specialty_id = VALUES(specialty_id),
    consultation_fee = VALUES(consultation_fee),
    status = VALUES(status);

-- 5. Thêm Users mới cho Đợt test Real Flow
INSERT INTO users (id, email, password_hash, full_name, phone, role, clinic_id, status)
VALUES
(14, 'doctor.minh@careslot.vn', '123456', 'BS. Trần Hoàng Minh', '0900000014', 'DOCTOR', 1, 'ACTIVE'),
(15, 'nguyenminhanh@example.com', '123456', 'Nguyễn Minh Anh', '0901234567', 'PATIENT', NULL, 'ACTIVE'),
(16, 'doctor.thao@careslot.vn', '123456', 'BS. Lê Thị Phương Thảo', '0900000016', 'DOCTOR', 1, 'ACTIVE'),
(17, 'doctor.bac@careslot.vn', '123456', 'BS. Nguyễn Hoài Bắc', '0900000017', 'DOCTOR', 1, 'ACTIVE'),
(19, 'doctor.minhkhai@careslot.vn', '123456', 'TS.BS Nguyễn Thị Minh Khai', '0900000019', 'DOCTOR', 1, 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), role = VALUES(role), clinic_id = VALUES(clinic_id);

-- 6. Thêm Patient Profile cho Bệnh nhân Nguyễn Minh Anh (Profile ID = 10)
INSERT INTO patient_profiles (id, user_id, full_name, date_of_birth, gender, phone, relationship, status)
VALUES (10, 15, 'Nguyễn Minh Anh', '1988-06-15', 'MALE', '0901234567', 'SELF', 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- 7. Thêm các phòng khám Consultation
INSERT INTO rooms (id, clinic_id, department_id, room_number, name, room_type, status) VALUES
(6, 1, 1, '306', 'Phòng khám Tiêu Hóa 306', 'CONSULTATION', 'ACTIVE'),
(7, 1, 1, '202', 'Phòng khám Nam học 202', 'CONSULTATION', 'ACTIVE'),
(8, 1, 1, '201', 'Phòng khám Sản 201', 'CONSULTATION', 'ACTIVE')
ON DUPLICATE KEY UPDATE name = VALUES(name), room_type = VALUES(room_type), status = VALUES(status);

-- 8. Tạo các Slot khám mẫu ban đầu (13/09 & 14/09)
INSERT INTO appointment_slots (id, doctor_id, appointment_date, start_time, end_time, room_name, status, room_id)
VALUES
(20, 1, '2026-09-16', '09:00:00', '09:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(21, 1, '2026-09-16', '09:20:00', '09:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(22, 1, '2026-09-16', '09:40:00', '10:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(23, 1, '2026-09-16', '10:00:00', '10:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(24, 1, '2026-09-16', '10:20:00', '10:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(25, 1, '2026-09-16', '10:40:00', '11:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(26, 1, '2026-09-16', '11:00:00', '11:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(27, 1, '2026-09-16', '11:20:00', '11:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(28, 1, '2026-09-16', '11:40:00', '12:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(29, 1, '2026-09-16', '12:00:00', '12:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(30, 1, '2026-09-16', '12:20:00', '12:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(31, 1, '2026-09-16', '12:40:00', '13:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(32, 1, '2026-09-16', '13:00:00', '13:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(33, 1, '2026-09-16', '13:20:00', '13:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(34, 1, '2026-09-16', '13:40:00', '14:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(35, 1, '2026-09-16', '14:20:00', '14:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(36, 1, '2026-09-16', '14:40:00', '15:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(37, 1, '2026-09-16', '15:00:00', '15:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(38, 1, '2026-09-16', '15:20:00', '15:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(39, 1, '2026-09-16', '15:40:00', '16:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),

(40, 1, '2026-09-15', '09:00:00', '09:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(41, 1, '2026-09-15', '09:20:00', '09:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(42, 1, '2026-09-15', '09:40:00', '10:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(43, 1, '2026-09-15', '10:00:00', '10:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(44, 1, '2026-09-15', '10:20:00', '10:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(45, 1, '2026-09-15', '10:40:00', '11:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(46, 1, '2026-09-15', '11:00:00', '11:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(47, 1, '2026-09-15', '11:20:00', '11:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(48, 1, '2026-09-15', '11:40:00', '12:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(49, 1, '2026-09-15', '12:00:00', '12:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(50, 1, '2026-09-15', '12:20:00', '12:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(51, 1, '2026-09-15', '12:40:00', '13:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(52, 1, '2026-09-15', '13:00:00', '13:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(53, 1, '2026-09-15', '13:20:00', '13:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(54, 1, '2026-09-15', '13:40:00', '14:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(55, 1, '2026-09-15', '14:20:00', '14:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(56, 1, '2026-09-15', '14:40:00', '15:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(57, 1, '2026-09-15', '15:00:00', '15:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(58, 1, '2026-09-15', '15:20:00', '15:40:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),
(59, 1, '2026-09-15', '15:40:00', '16:00:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 1),

(60, 11, '2026-09-15', '01:20:00', '01:40:00', 'Phòng 306 Tiêu Hóa', 'AVAILABLE', 6),
(61, 11, '2026-09-15', '01:40:00', '02:00:00', 'Phòng 306 Tiêu Hóa', 'AVAILABLE', 6),
(62, 11, '2026-09-15', '09:00:00', '09:20:00', 'Phòng 306 Tiêu Hóa', 'AVAILABLE', 6),
(63, 11, '2026-09-15', '09:20:00', '09:40:00', 'Phòng 306 Tiêu Hóa', 'AVAILABLE', 6),
(64, 11, '2026-09-15', '09:40:00', '10:00:00', 'Phòng 306 Tiêu Hóa', 'AVAILABLE', 6),
(65, 11, '2026-09-15', '10:00:00', '10:20:00', 'Phòng 306 Tiêu Hóa', 'AVAILABLE', 6),
(66, 11, '2026-09-15', '13:40:00', '14:00:00', 'Phòng 306 Tiêu Hóa', 'AVAILABLE', 6),
(67, 11, '2026-09-15', '14:20:00', '14:40:00', 'Phòng 306 Tiêu Hóa', 'AVAILABLE', 6),
(68, 11, '2026-09-15', '14:40:00', '15:00:00', 'Phòng 306 Tiêu Hóa', 'AVAILABLE', 6),
(69, 11, '2026-09-15', '17:40:00', '18:00:00', 'Phòng 306 Tiêu Hóa', 'AVAILABLE', 6),
(70, 11, '2026-09-15', '11:00:00', '11:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 6),
(71, 11, '2026-09-15', '12:00:00', '12:20:00', 'Phòng 305 Tiêu Hóa', 'AVAILABLE', 6)
ON DUPLICATE KEY UPDATE doctor_id = VALUES(doctor_id);

