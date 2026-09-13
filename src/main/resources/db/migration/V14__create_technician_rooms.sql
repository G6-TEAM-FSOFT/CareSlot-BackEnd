-- V14__create_technician_rooms.sql
-- Tạo bảng technician_rooms liên kết Kỹ thuật viên với Phòng Cận lâm sàng

CREATE TABLE IF NOT EXISTS technician_rooms (
    user_id BIGINT UNSIGNED NOT NULL,
    room_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (user_id, room_id),
    CONSTRAINT fk_technician_rooms_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_technician_rooms_room FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thêm tài khoản KTV thứ 3 cho Chụp CT (User ID = 18)
INSERT INTO users (id, email, password_hash, full_name, phone, role, clinic_id, status)
VALUES (18, 'tech.ct@careslot.vn', '123456', 'Nguyễn Văn Minh (KTV Chụp CT)', '0900000018', 'TECHNICIAN', 1, 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), role = VALUES(role), clinic_id = VALUES(clinic_id);

-- Cập nhật thông tin mô tả tên KTV cho rõ ràng
UPDATE users SET full_name = 'Phạm Quốc Hùng (KTV Siêu Âm)' WHERE id = 13;

-- Phân công KTV phụ trách phòng cố định (1 Technician - 1 Room)
-- User 12 (tech.lab)        -> Room 2 (Phòng A101 Xét Nghiệm)
-- User 13 (tech.imaging)    -> Room 3 (Phòng B201 Siêu Âm)
-- User 18 (tech.ct)         -> Room 4 (Phòng B202 Chụp CT)
INSERT INTO technician_rooms (user_id, room_id) VALUES
(12, 2),
(13, 3),
(18, 4)
ON DUPLICATE KEY UPDATE room_id = VALUES(room_id);
