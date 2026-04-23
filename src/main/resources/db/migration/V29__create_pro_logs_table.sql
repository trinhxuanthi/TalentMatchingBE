-- V29: Tạo bảng lưu vết lịch sử sử dụng tính năng PRO để thống kê biểu đồ
CREATE TABLE pro_feature_logs (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  user_id BIGINT NOT NULL,
                                  feature_name VARCHAR(50) NOT NULL,
                                  used_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                                  INDEX idx_log_feature (feature_name),
                                  INDEX idx_log_date (used_at),
                                  CONSTRAINT fk_pro_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);