-- V30: Thêm cờ đánh dấu Job của tài khoản PRO để đẩy lên Top
ALTER TABLE jobs
    ADD COLUMN is_priority BOOLEAN DEFAULT FALSE;

-- Cập nhật cờ này cho các Job hiện tại nếu chủ nhân là PRO (Tùy chọn)
-- UPDATE jobs j JOIN users u ON j.employer_id = u.id
-- SET j.is_priority = TRUE WHERE u.account_type = 'PRO';