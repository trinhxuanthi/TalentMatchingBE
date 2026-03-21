-- =========================================================================
-- MIGRATION V11: Cập nhật bảng Jobs theo chuẩn cấu trúc TopCV & AI
-- =========================================================================

-- 1. Thêm cột cờ đánh dấu Lương thỏa thuận
ALTER TABLE jobs
    ADD COLUMN is_salary_negotiable BOOLEAN DEFAULT FALSE;

-- 2. Thêm các cột cho khối Sidebar (Thông tin chung)
ALTER TABLE jobs
    ADD COLUMN job_level VARCHAR(255) NULL COMMENT 'Cấp bậc (VD: Quản lý / Giám sát)',
ADD COLUMN education_level VARCHAR(255) NULL COMMENT 'Học vấn (VD: Đại học trở lên)',
ADD COLUMN quantity INT NULL COMMENT 'Số lượng tuyển';

-- 3. Thêm cột cho khối Văn bản hiển thị UI
ALTER TABLE jobs
    ADD COLUMN benefits TEXT NULL COMMENT 'Quyền lợi ứng viên (Hiển thị UI)';

-- 4. Thêm các cột JSON phục vụ cho Động cơ AI và Filter
ALTER TABLE jobs
    ADD COLUMN required_skills TEXT NULL COMMENT 'Lưu JSON kỹ năng AI (VD: {"java":3, "mysql":1})',
ADD COLUMN categories TEXT NULL COMMENT 'Lưu mảng JSON danh mục (VD: ["IT - Phần mềm"])';