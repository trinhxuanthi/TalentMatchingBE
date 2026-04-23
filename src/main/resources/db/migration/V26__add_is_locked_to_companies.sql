-- V25: Thêm cột is_locked vào bảng companies để Admin quản lý trạng thái khóa (mặc định là false/0)
ALTER TABLE companies
    ADD COLUMN is_locked BOOLEAN NOT NULL DEFAULT FALSE;