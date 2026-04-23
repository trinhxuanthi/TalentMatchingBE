-- V31: Tạo bảng lưu vết ai đã xem hồ sơ ứng viên
CREATE TABLE profile_views (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               candidate_id BIGINT NOT NULL,
                               employer_id BIGINT NOT NULL,
                               viewed_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    -- Index để query danh sách cho nhanh
                               INDEX idx_view_candidate (candidate_id),
                               INDEX idx_view_date (viewed_at),

    -- Khóa ngoại liên kết tới bảng users
                               CONSTRAINT fk_view_candidate FOREIGN KEY (candidate_id) REFERENCES users(id) ON DELETE CASCADE,
                               CONSTRAINT fk_view_employer FOREIGN KEY (employer_id) REFERENCES users(id) ON DELETE CASCADE
);