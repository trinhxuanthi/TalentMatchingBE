-- Tạo bảng candidate_rankings
CREATE TABLE candidate_rankings (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    session_id VARCHAR(100) NOT NULL,
                                    candidate_file VARCHAR(255) NOT NULL,
                                    match_score INT,
                                    recommendation VARCHAR(50),
                                    exp_score INT,
                                    skill_score INT,
                                    role_score INT,
                                    detailed_analysis_json LONGTEXT, -- Dùng LONGTEXT cho an toàn với MySQL
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 🔥 VŨ KHÍ TỐI ƯU TỐC ĐỘ ĐỌC (READ)
CREATE INDEX idx_rankings_session_score
    ON candidate_rankings (session_id, match_score);