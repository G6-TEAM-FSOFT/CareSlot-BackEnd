-- ============================================================
-- V7__add_clinic_staff_and_partner_users.sql
-- CareSlot Seed Clinic Staff & Partner Accounts
-- ============================================================

INSERT INTO users (id, email, password_hash, full_name, phone, role, clinic_id, status)
VALUES
(5, 'staff1@careslot.vn', '123456', 'Nhân viên BV ĐHYHN - Tôn Thất Tùng', '0900000005', 'CLINIC_STAFF', 1, 'ACTIVE'),
(6, 'partner.caugiay@careslot.vn', '123456', 'Quản lý BV ĐHYHN - Cầu Giấy', '0900000006', 'CLINIC_PARTNER', 2, 'ACTIVE'),
(7, 'staff.caugiay@careslot.vn', '123456', 'Nhân viên BV ĐHYHN - Cầu Giấy', '0900000007', 'CLINIC_STAFF', 2, 'ACTIVE'),
(8, 'partner.hoangmai@careslot.vn', '123456', 'Quản lý BV ĐHYHN - Hoàng Mai', '0900000008', 'CLINIC_PARTNER', 3, 'ACTIVE'),
(9, 'staff.hoangmai@careslot.vn', '123456', 'Nhân viên BV ĐHYHN - Hoàng Mai', '0900000009', 'CLINIC_STAFF', 3, 'ACTIVE')
ON DUPLICATE KEY UPDATE
    email = VALUES(email),
    password_hash = VALUES(password_hash),
    full_name = VALUES(full_name),
    phone = VALUES(phone),
    role = VALUES(role),
    clinic_id = VALUES(clinic_id),
    status = VALUES(status);
