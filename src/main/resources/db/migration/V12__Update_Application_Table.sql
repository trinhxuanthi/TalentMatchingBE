-- =========================================================================
-- MIGRATION V12: Bổ sung các trường dữ liệu Form (Lọc cứng) cho Application
-- =========================================================================

-- Thêm các trường thông tin cá nhân (phòng khi ứng viên dùng số/email khác profile)
ALTER TABLE applications
    ADD COLUMN full_name VARCHAR(255) NULL,
ADD COLUMN email VARCHAR(255) NULL,
ADD COLUMN phone VARCHAR(20) NULL;

-- Thêm các trường Lọc Cứng để Java query tốc độ cao
ALTER TABLE applications
    ADD COLUMN education_level VARCHAR(255) NULL COMMENT 'Trình độ học vấn (VD: Đại học)',
ADD COLUMN years_of_experience INT NULL COMMENT 'Số năm kinh nghiệm (Lưu số nguyên)';

-- Cập nhật dữ liệu cũ (nếu có) để tránh lỗi Null
UPDATE applications
SET full_name = 'Chưa cập nhật', email = 'Chưa cập nhật', phone = 'Chưa cập nhật'
WHERE full_name IS NULL;