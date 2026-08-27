-- ============================================================
-- V2__insert_demo_data.sql
-- CareSlot Demo Seed Data
-- MySQL 8+
-- ============================================================
--
-- Purpose:
-- Seed dữ liệu demo phục vụ development/testing.
--
-- Lưu ý:
-- - Clinic / Specialty / Doctor dựa trên dữ liệu tham khảo công khai.
-- - Giá khám, User, PatientProfile, Slot, Appointment là DEMO DATA.
-- - IDs được khai báo cố định để dễ seed và kiểm thử quan hệ.
-- - Migration này giả định chạy ngay sau V1 trên database mới.
-- ============================================================


-- ============================================================
-- 1. CLINICS
-- ============================================================

INSERT INTO clinics (id,
                     name,
                     address,
                     latitude,
                     longitude,
                     phone,
                     description,
                     status)
VALUES (1,
        'BV Đại học Y Hà Nội - Cơ sở Tôn Thất Tùng',
        'Số 1 Tôn Thất Tùng, Phường Trung Tự, Quận Đống Đa, Hà Nội',
        NULL,
        NULL,
        '19006422',
        'Cơ sở khám bệnh tại Tôn Thất Tùng của Bệnh viện Đại học Y Hà Nội.',
        'ACTIVE'),
       (2,
        'BV Đại học Y Hà Nội - Cơ sở Cầu Giấy',
        'Số 10 Trương Công Giai, Cầu Giấy, Hà Nội',
        NULL,
        NULL,
        '19006422',
        'Cơ sở khám bệnh tại Cầu Giấy của Bệnh viện Đại học Y Hà Nội.',
        'ACTIVE'),
       (3,
        'BV Đại học Y Hà Nội - Cơ sở Hoàng Mai',
        'Số 587 Tam Trinh, Yên Sở, Hoàng Mai, Hà Nội',
        NULL,
        NULL,
        '19006422',
        'Cơ sở khám bệnh tại Hoàng Mai của Bệnh viện Đại học Y Hà Nội.',
        'ACTIVE');


-- ============================================================
-- 2. SPECIALTIES
-- ============================================================

INSERT INTO specialties (id,
                         clinic_id,
                         name,
                         description,
                         status)
VALUES (1,
        1,
        'Hỗ trợ sinh sản',
        'Khám, tư vấn và điều trị trong lĩnh vực hỗ trợ sinh sản.',
        'ACTIVE'),
       (2,
        1,
        'Nam học và Y học Giới tính',
        'Khám, tư vấn và điều trị các vấn đề thuộc Nam học và Y học giới tính.',
        'ACTIVE'),
       (3,
        1,
        'Chẩn đoán hình ảnh và Can thiệp điện quang',
        'Chẩn đoán bệnh lý bằng các phương pháp hình ảnh và thực hiện các kỹ thuật can thiệp điện quang.',
        'ACTIVE');


-- ============================================================
-- 3. USERS
-- ============================================================
--
-- Demo accounts:
--
-- 1. CareSlot Admin
-- 2. Clinic Partner
-- 3. Patient Nguyễn Văn An
-- 4. Patient Trần Thị Bình
--
-- password_hash hiện là dữ liệu placeholder.
-- Khi tích hợp Spring Security BCrypt, thay bằng BCrypt hash
-- được sinh từ password demo của project.
-- ============================================================

INSERT INTO users (id,
                   email,
                   password_hash,
                   full_name,
                   phone,
                   role,
                   clinic_id,
                   status)
VALUES (1,
        'admin@careslot.vn',
        'DEMO_PASSWORD_HASH',
        'CareSlot Admin',
        '0900000001',
        'ADMIN',
        NULL,
        'ACTIVE'),
       (2,
        'clinic1@careslot.vn',
        'DEMO_PASSWORD_HASH',
        'Quản lý BV Đại học Y Hà Nội',
        '0900000002',
        'CLINIC_PARTNER',
        1,
        'ACTIVE'),
       (3,
        'nguyenvanan@example.com',
        'DEMO_PASSWORD_HASH',
        'Nguyễn Văn An',
        '0901000001',
        'PATIENT',
        NULL,
        'ACTIVE'),
       (4,
        'tranthibinh@example.com',
        'DEMO_PASSWORD_HASH',
        'Trần Thị Bình',
        '0901000002',
        'PATIENT',
        NULL,
        'ACTIVE');


-- ============================================================
-- 4. PATIENT PROFILES
--
-- User 1 ---- N PatientProfile
--
-- User #3 có:
-- - bản thân
-- - con
-- - mẹ
--
-- để demo chức năng Family Profile.
-- ============================================================

INSERT INTO patient_profiles (id,
                              user_id,
                              full_name,
                              date_of_birth,
                              gender,
                              phone,
                              relationship,
                              status)
VALUES (1,
        3,
        'Nguyễn Văn An',
        '2000-05-12',
        'MALE',
        '0901000001',
        'SELF',
        'ACTIVE'),
       (2,
        3,
        'Nguyễn Minh Anh',
        '2015-09-20',
        'FEMALE',
        NULL,
        'CHILD',
        'ACTIVE'),
       (3,
        3,
        'Nguyễn Thị Hoa',
        '1970-03-16',
        'FEMALE',
        '0901000011',
        'MOTHER',
        'ACTIVE'),
       (4,
        4,
        'Trần Thị Bình',
        '1998-08-24',
        'FEMALE',
        '0901000002',
        'SELF',
        'ACTIVE');


-- ============================================================
-- 5. DOCTORS
--
-- Clinic    1 ---- N Doctor
-- Specialty 1 ---- N Doctor
--
-- consultation_fee là DEMO DATA.
-- ============================================================

INSERT INTO doctors (id,
                     clinic_id,
                     specialty_id,
                     full_name,
                     title,
                     bio,
                     avatar_url,
                     consultation_fee,
                     status)
VALUES (1,
        1,
        1,
        'Nguyễn Mạnh Hà',
        'PGS.TS',
        'Bác sĩ chuyên ngành Hỗ trợ sinh sản.',
        NULL,
        500000,
        'ACTIVE'),
       (2,
        1,
        1,
        'Nguyễn Thị Minh Khai',
        'TS.BS',
        'Bác sĩ chuyên ngành Hỗ trợ sinh sản.',
        NULL,
        400000,
        'ACTIVE'),
       (3,
        1,
        1,
        'Nguyễn Phúc Hoàn',
        'TS.BS',
        'Bác sĩ chuyên ngành Hỗ trợ sinh sản.',
        NULL,
        400000,
        'ACTIVE'),
       (4,
        1,
        1,
        'Đỗ Thùy Hương',
        'ThS.BSNT',
        'Bác sĩ chuyên ngành Hỗ trợ sinh sản.',
        NULL,
        350000,
        'ACTIVE'),
       (5,
        1,
        1,
        'Trịnh Thị Ngọc Yến',
        'ThS.BSNT',
        'Bác sĩ chuyên ngành Hỗ trợ sinh sản.',
        NULL,
        350000,
        'ACTIVE'),
       (6,
        1,
        2,
        'Nguyễn Hoài Bắc',
        'PGS.TS.BS',
        'Bác sĩ chuyên ngành Nam học và Y học Giới tính.',
        NULL,
        500000,
        'ACTIVE'),
       (7,
        1,
        2,
        'Phạm Minh Quân',
        'ThS.BS',
        'Bác sĩ chuyên ngành Nam học và Y học Giới tính.',
        NULL,
        300000,
        'ACTIVE'),
       (8,
        1,
        2,
        'Nguyễn Xuân Đức Hoàng',
        'ThS.BSNT',
        'Bác sĩ chuyên ngành Nam học và Y học Giới tính.',
        NULL,
        350000,
        'ACTIVE'),
       (9,
        1,
        3,
        'Lê Tuấn Linh',
        'PGS.TS.BSNT',
        'Bác sĩ chuyên ngành Chẩn đoán hình ảnh và Can thiệp điện quang.',
        NULL,
        500000,
        'ACTIVE'),
       (10,
        1,
        3,
        'Đoàn Tiến Lưu',
        'PGS.TS.BSNT',
        'Bác sĩ chuyên ngành Chẩn đoán hình ảnh và Can thiệp điện quang.',
        NULL,
        500000,
        'ACTIVE');


-- ============================================================
-- 6. APPOINTMENT SLOTS
--
-- Doctor 1 ---- N AppointmentSlot
--
-- Slot là DEMO DATA.
--
-- Slot #3 minh họa:
-- CANCELLED Appointment
--          ↓
-- slot được mở lại
--          ↓
-- CONFIRMED Appointment mới
--
-- vì:
-- AppointmentSlot 1 ---- 0..N Appointment
-- ============================================================

INSERT INTO appointment_slots (id,
                               doctor_id,
                               appointment_date,
                               start_time,
                               end_time,
                               room_name,
                               status)
VALUES (1,
        1,
        '2026-09-01',
        '08:00:00',
        '08:30:00',
        'Tầng 3 - Nhà A3',
        'AVAILABLE'),
       (2,
        1,
        '2026-09-01',
        '08:30:00',
        '09:00:00',
        'Tầng 3 - Nhà A3',
        'AVAILABLE'),
       (3,
        1,
        '2026-09-01',
        '09:00:00',
        '09:30:00',
        'Tầng 3 - Nhà A3',
        'BOOKED'),
       (4,
        2,
        '2026-09-01',
        '08:00:00',
        '08:30:00',
        'Tầng 3 - Nhà A3',
        'AVAILABLE'),
       (5,
        2,
        '2026-09-01',
        '08:30:00',
        '09:00:00',
        'Tầng 3 - Nhà A3',
        'HELD'),
       (6,
        6,
        '2026-09-02',
        '13:30:00',
        '14:00:00',
        NULL,
        'BOOKED'),
       (7,
        6,
        '2026-09-02',
        '14:00:00',
        '14:30:00',
        NULL,
        'AVAILABLE'),
       (8,
        7,
        '2026-09-02',
        '15:00:00',
        '15:30:00',
        NULL,
        'AVAILABLE');


-- ============================================================
-- 7. APPOINTMENTS
--
-- PatientProfile  1 ---- N Appointment
-- AppointmentSlot 1 ---- 0..N Appointment
--
-- Appointment #1 và #2 cùng slot #3 để minh họa:
--
-- Slot #3
--   |
--   +-- Appointment #1 : CANCELLED
--   |
--   +-- Appointment #2 : CONFIRMED
--
-- Đây là trường hợp slot được tái sử dụng sau cancellation.
-- ============================================================

INSERT INTO appointments (id,
                          booking_code,
                          patient_profile_id,
                          slot_id,
                          symptom_note,
                          consultation_fee,
                          deposit_amount,
                          status,
                          created_at,
                          approved_at,
                          approved_by,
                          rejected_at,
                          cancelled_at,
                          checked_in_at)
VALUES

-- ------------------------------------------------------------
-- Appointment #1
-- Booking cũ bị Patient hủy.
-- ------------------------------------------------------------
(1,
 'CS260901001',
 1,
 3,
 'Tư vấn sức khỏe sinh sản.',
 500000,
 100000,
 'CANCELLED',
 '2026-08-28 08:00:00',
 NULL,
 NULL,
 NULL,
 '2026-08-28 09:00:00',
 NULL),

-- ------------------------------------------------------------
-- Appointment #2
-- Slot #3 được mở lại và Patient khác booking thành công.
-- ------------------------------------------------------------
(2,
 'CS260901002',
 4,
 3,
 'Khám và tư vấn hỗ trợ sinh sản.',
 500000,
 100000,
 'CONFIRMED',
 '2026-08-28 10:00:00',
 '2026-08-28 10:05:00',
 2,
 NULL,
 NULL,
 NULL),

-- ------------------------------------------------------------
-- Appointment #3
-- Patient đang giữ slot và chưa thanh toán deposit.
-- ------------------------------------------------------------
(3,
 'CS260901003',
 2,
 5,
 'Khám theo lịch hẹn.',
 400000,
 100000,
 'PENDING_PAYMENT',
 '2026-08-28 11:00:00',
 NULL,
 NULL,
 NULL,
 NULL,
 NULL),

-- ------------------------------------------------------------
-- Appointment #4
-- Booking Nam học đã được xác nhận.
-- ------------------------------------------------------------
(4,
 'CS260902001',
 1,
 6,
 'Khám Nam học.',
 500000,
 100000,
 'CONFIRMED',
 '2026-08-28 13:00:00',
 '2026-08-28 13:05:00',
 2,
 NULL,
 NULL,
 NULL);