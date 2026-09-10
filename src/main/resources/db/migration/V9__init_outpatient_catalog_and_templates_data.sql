-- ============================================================
-- CareSlot Migration V9: Init Outpatient Seed Data & Test Demo
-- ============================================================

-- 1. Thêm Users mới cho các Roles mới (6 Roles System)
INSERT INTO users (id, email, password_hash, full_name, phone, role, clinic_id, status)
VALUES
(10, 'receptionist.tonthattung@careslot.vn', '123456', 'Lê Thị Mai (Lễ tân & Thu ngân)', '0900000010', 'RECEPTIONIST', 1, 'ACTIVE'),
(11, 'assistant.gastro@careslot.vn', '123456', 'Nguyễn Thu Hà (Trợ lý Tiêu Hóa)', '0900000011', 'CLINICAL_ASSISTANT', 1, 'ACTIVE'),
(12, 'tech.lab@careslot.vn', '123456', 'Trần Văn Bình (KTV Xét nghiệm)', '0900000012', 'TECHNICIAN', 1, 'ACTIVE'),
(13, 'tech.imaging@careslot.vn', '123456', 'Phạm Quốc Hùng (KTV Chẩn đoán hình ảnh)', '0900000013', 'TECHNICIAN', 1, 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- 2. Thêm Departments
INSERT INTO departments (id, clinic_id, code, name, dept_type, status) VALUES
(1, 1, 'GASTRO', 'Khoa Tiêu Hóa', 'CLINICAL', 'ACTIVE'),
(2, 1, 'LAB', 'Khoa Xét Nghiệm', 'LABORATORY', 'ACTIVE'),
(3, 1, 'IMAGING', 'Khoa Chẩn Đoán Hình Ảnh', 'IMAGING', 'ACTIVE'),
(4, 1, 'RECEPTION', 'Khoa Tiếp Nhận & Thu Ngân', 'RECEPTION', 'ACTIVE')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3. Thêm Rooms
INSERT INTO rooms (id, clinic_id, department_id, room_number, name, room_type, status) VALUES
(1, 1, 1, '305', 'Phòng khám Tiêu Hóa 305', 'CONSULTATION', 'ACTIVE'),
(2, 1, 2, 'A101', 'Phòng Lấy Mẫu Xét Nghiệm A101', 'LAB_COLLECTION', 'ACTIVE'),
(3, 1, 3, 'B201', 'Phòng Siêu Âm B201', 'ULTRASOUND', 'ACTIVE'),
(4, 1, 3, 'B202', 'Phòng Chụp CT B202', 'CT', 'ACTIVE'),
(5, 1, 4, 'R101', 'Quầy Lễ Tân & Thu Ngân 01', 'CASHIER', 'ACTIVE')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 4. Update room_id cho các AppointmentSlots mẫu ở V2
UPDATE appointment_slots SET room_id = 1 WHERE doctor_id = 1 OR id = 3;

-- 5. Result Templates
INSERT INTO result_templates (id, code, name, service_type, template_schema, status) VALUES
(1, 'RT_CBC', 'Mẫu Kết Quả Công Thức Máu', 'LABORATORY', '{
    "fields": [
        {"key": "wbc", "label": "Bạch cầu (WBC)", "unit": "G/L", "reference": "4.0 - 10.0"},
        {"key": "rbc", "label": "Hồng cầu (RBC)", "unit": "T/L", "reference": "3.8 - 5.8"},
        {"key": "hgb", "label": "Huyết sắc tố (Hb)", "unit": "g/L", "reference": "120 - 165"},
        {"key": "plt", "label": "Tiểu cầu (PLT)", "unit": "G/L", "reference": "150 - 450"}
    ]
}', 'ACTIVE'),
(2, 'RT_US_ABD', 'Mẫu Kết Quả Siêu Âm Ổ Bụng', 'IMAGING', '{
    "fields": [
        {"key": "liver", "label": "Gan", "type": "text"},
        {"key": "gallbladder", "label": "Túi mật", "type": "text"},
        {"key": "pancreas", "label": "Tụy", "type": "text"},
        {"key": "spleen", "label": "Lách", "type": "text"},
        {"key": "kidneys", "label": "Hai thận", "type": "text"}
    ]
}', 'ACTIVE'),
(3, 'RT_CT_ABD', 'Mẫu Kết Quả Chụp CT Ổ Bụng', 'IMAGING', '{
    "fields": [
        {"key": "technique", "label": "Kỹ thuật chụp", "type": "text"},
        {"key": "findings", "label": "Mô tả tổn thương", "type": "text"}
    ]
}', 'ACTIVE')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 6. Medical Record Templates
INSERT INTO medical_record_templates (id, specialty_id, name, version, template_schema, status) VALUES
(1, 1, 'Mẫu Bệnh Án Ngoại Trú Tiêu Hóa', 1, '{
    "sections": [
        {"key": "chief_complaint", "label": "Lý do đến khám", "type": "text", "required": true},
        {"key": "hpi", "label": "Bệnh sử & Triệu chứng", "type": "textarea", "required": true},
        {"key": "medical_history", "label": "Tiền sử bệnh bản thân & gia đình", "type": "textarea"},
        {"key": "abdominal_exam", "label": "Khám bụng lâm sàng", "type": "textarea"}
    ]
}', 'ACTIVE')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 7. Service Catalog (Mỗi Service map với 1 default_room_id cố định duy nhất)
INSERT INTO service_catalog (id, clinic_id, department_id, code, name, service_type, price, payment_policy, default_room_id, result_template_id, status) VALUES
(1, 1, 2, 'CBC', 'Công thức máu toàn phần (CBC)', 'LABORATORY', 120000.00, 'PREPAID', 2, 1, 'ACTIVE'),
(2, 1, 3, 'US_ABDOMEN', 'Siêu âm ổ bụng tổng quát', 'IMAGING', 250000.00, 'PREPAID', 3, 2, 'ACTIVE'),
(3, 1, 3, 'CT_ABDOMEN', 'Chụp vi tính cắt lớp (CT) ổ bụng', 'IMAGING', 1200000.00, 'PREPAID', 4, 3, 'ACTIVE')
ON DUPLICATE KEY UPDATE name = VALUES(name);
