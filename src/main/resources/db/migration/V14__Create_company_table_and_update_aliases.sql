CREATE TABLE companies (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           name VARCHAR(255) NOT NULL,
                           tax_code VARCHAR(100) NOT NULL UNIQUE,
                           address VARCHAR(255),
                           website VARCHAR(255),
                           business_license_url VARCHAR(500),
                           hr_position VARCHAR(100),
                           approval_status VARCHAR(50) DEFAULT 'PENDING',
                           reject_reason VARCHAR(500),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Ràng buộc: Mỗi công ty thuộc về 1 User, khi User bị xóa thì Công ty cũng bị xóa theo
                           CONSTRAINT fk_companies_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                           CONSTRAINT uk_companies_user_id UNIQUE (user_id)
);