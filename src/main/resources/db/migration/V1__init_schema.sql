CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(191) NOT NULL UNIQUE,
                       password VARCHAR(255) NULL,         -- NULL để hỗ trợ Login Google
                       fullname VARCHAR(255),
                       role VARCHAR(50) NOT NULL,          -- CANDIDATE, EMPLOYER, ADMIN
                       avatar VARCHAR(500),                -- Link từ Cloudinary
                       phone_number VARCHAR(20),           -- Cần cho cả Employer và Candidate liên hệ
                       is_active BOOLEAN DEFAULT TRUE,     -- Để khóa tài khoản khi vi phạm
                       provider VARCHAR(20) DEFAULT 'LOCAL', -- 'LOCAL' (đăng ký web) hoặc 'GOOGLE'

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       INDEX idx_user_email (email),
                       INDEX idx_user_role (role)           -- Tối ưu khi Admin lọc danh sách Employer/Candidate
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;