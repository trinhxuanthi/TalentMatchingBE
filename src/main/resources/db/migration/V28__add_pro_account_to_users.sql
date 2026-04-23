-- V28: Thêm cột phân loại tài khoản và ngày hết hạn PRO vào bảng users
ALTER TABLE users
    ADD COLUMN account_type ENUM('BASIC', 'PRO') NOT NULL DEFAULT 'BASIC',
ADD COLUMN pro_expired_at DATETIME DEFAULT NULL;