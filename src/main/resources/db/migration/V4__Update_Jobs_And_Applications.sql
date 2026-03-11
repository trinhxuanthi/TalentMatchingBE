-- ==========================================================
-- 1. CẬP NHẬT BẢNG JOBS
-- ==========================================================
-- Thêm Deadline để quản lý hạn nộp đơn
ALTER TABLE jobs
    ADD COLUMN deadline DATETIME AFTER experience_level;

-- Thêm Requirements để tách riêng yêu cầu (Giúp AI Matching chuẩn hơn) [cite: 2026-03-07]
ALTER TABLE jobs
    ADD COLUMN requirements TEXT AFTER description;

-- Thêm is_active để hỗ trợ xóa mềm (Soft Delete)
ALTER TABLE jobs
    ADD COLUMN is_active BOOLEAN DEFAULT TRUE AFTER status;

-- Tối ưu Index cho tính năng tìm kiếm tổ hợp (Location + Status + Deadline)
CREATE INDEX idx_job_filter_composite ON jobs(location, status, deadline);


-- ==========================================================
-- 2. CẬP NHẬT BẢNG APPLICATIONS
-- ==========================================================
-- Thêm is_viewed để Employer quản lý đơn mới/đã xem [cite: 2026-03-10]
ALTER TABLE applications
    ADD COLUMN is_viewed BOOLEAN DEFAULT FALSE AFTER status;

-- Index giúp truy vấn số lượng đơn chưa đọc cực nhanh
CREATE INDEX idx_app_unread_count ON applications(job_id, is_viewed);


-- ==========================================================
-- 3. CHỈNH SỬA DỮ LIỆU CŨ (Nếu cần)
-- ==========================================================
-- Gán deadline mặc định cho các job cũ (ví dụ 30 ngày sau khi tạo) để tránh lỗi lọc
UPDATE jobs SET deadline = DATE_ADD(created_at, INTERVAL 30 DAY) WHERE deadline IS NULL;