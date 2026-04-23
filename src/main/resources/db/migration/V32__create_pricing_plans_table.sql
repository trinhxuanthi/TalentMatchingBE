-- V32: Bảng lưu các gói cước PRO (Tùy chỉnh giá, giảm giá, phân loại User)
CREATE TABLE pricing_plans (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               name VARCHAR(100) NOT NULL,
                               description TEXT,
                               duration_days INT NOT NULL,          -- Số ngày hiệu lực của gói (30, 365)
                               base_price DECIMAL(19, 2) NOT NULL,  -- Giá gốc (VND)
                               discount_percent INT DEFAULT 0,      -- Phần trăm giảm giá (0-100)
                               target_role VARCHAR(20) NOT NULL,    -- Phân loại: 'EMPLOYER' hoặc 'CANDIDATE'
                               is_active BOOLEAN DEFAULT TRUE,
                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- DATA MẪU ĐỂ SẾP TEST LUÔN:
-- 1. Gói Doanh Nghiệp (HR): Giá 2 triệu, đang giảm 10%
INSERT INTO pricing_plans (name, description, duration_days, base_price, discount_percent, target_role)
VALUES ('PRO Doanh Nghiệp 1 Tháng', 'Đăng tin không giới hạn, Tải CV PDF xịn', 30, 2000000, 10, 'EMPLOYER');

-- 2. Gói Ứng Viên (Candidate): Giá 99k, không giảm
INSERT INTO pricing_plans (name, description, duration_days, base_price, discount_percent, target_role)
VALUES ('PRO Ứng Viên VIP', 'Xem ai đã xem hồ sơ, Lên Top tìm kiếm', 30, 99000, 0, 'CANDIDATE');