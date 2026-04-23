-- 1. Tạo bảng candidate_profiles (Lưu dữ liệu AI bóc tách)
CREATE TABLE candidate_profiles (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    user_id BIGINT NOT NULL UNIQUE,
                                    standard_title VARCHAR(255),
                                    skills TEXT,
                                    years_of_experience INT,
                                    CONSTRAINT fk_candidate_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 2. Tạo bảng job_matches (Lưu kết quả điểm số Vector)
CREATE TABLE job_matches (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             candidate_id BIGINT NOT NULL,
                             job_id BIGINT NOT NULL,
                             match_score DOUBLE NOT NULL,
                             skill_score DOUBLE,
                             title_score DOUBLE,
                             exp_score DOUBLE,
                             is_notified BOOLEAN NOT NULL DEFAULT FALSE,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             CONSTRAINT fk_match_candidate FOREIGN KEY (candidate_id) REFERENCES users(id) ON DELETE CASCADE,
                             CONSTRAINT fk_match_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);