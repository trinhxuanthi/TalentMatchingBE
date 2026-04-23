CREATE TABLE reports (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         sender_id BIGINT NOT NULL,
                         title VARCHAR(200) NOT NULL,
                         content TEXT NOT NULL,
                         type VARCHAR(50) NOT NULL,
                         status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                         admin_note TEXT,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         CONSTRAINT fk_report_sender FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- Tạo các index để Admin tìm kiếm và lọc báo cáo siêu tốc
CREATE INDEX idx_report_sender ON reports(sender_id);
CREATE INDEX idx_report_status ON reports(status);
CREATE INDEX idx_report_type ON reports(type);