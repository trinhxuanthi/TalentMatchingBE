CREATE TABLE saved_jobs (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            candidate_id BIGINT NOT NULL,
                            job_id BIGINT NOT NULL,
                            saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Xóa User hoặc xóa Job thì dòng lưu này cũng tự động biến mất
                            CONSTRAINT fk_saved_candidate FOREIGN KEY (candidate_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT fk_saved_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,

    -- Ràng buộc: Một người chỉ được lưu 1 Job duy nhất 1 lần (chống spam)
                            CONSTRAINT uk_candidate_job UNIQUE (candidate_id, job_id)
);