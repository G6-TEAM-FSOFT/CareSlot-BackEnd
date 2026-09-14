-- V13__link_doctors_to_users.sql
-- Thêm trường user_id vào bảng doctors và cập nhật liên kết cho dữ liệu seed

ALTER TABLE doctors
ADD COLUMN user_id BIGINT UNSIGNED NULL AFTER specialty_id;

ALTER TABLE doctors
ADD CONSTRAINT fk_doctors_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL;

-- Cập nhật liên kết user_id cho các Bác sĩ
UPDATE doctors SET user_id = 14 WHERE id = 1; -- BS. Trần Hoàng Minh (doctor.minh@careslot.vn)
UPDATE doctors SET user_id = 16 WHERE id = 11; -- BS. Lê Thị Phương Thảo (doctor.thao@careslot.vn)
UPDATE doctors SET user_id = 17 WHERE id = 6; -- BS. Nguyễn Hoài Bắc (doctor.bac@careslot.vn)
UPDATE doctors SET user_id = 19 WHERE id = 2; -- TS.BS Nguyễn Thị Minh Khai (doctor.minhkhai@careslot.vn)
