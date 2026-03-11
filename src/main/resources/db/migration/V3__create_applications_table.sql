CREATE TABLE applications (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              job_id BIGINT NOT NULL,
                              candidate_id BIGINT NOT NULL,
                              cv_url TEXT NOT NULL,
                              cover_letter TEXT,
                              status VARCHAR(20) DEFAULT 'PENDING' NOT NULL,

                              notes TEXT, -- Ghi chú của nhà tuyển dụng
                              is_active BOOLEAN DEFAULT TRUE,
                              applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              CONSTRAINT fk_app_job FOREIGN KEY (job_id) REFERENCES jobs(id),
                              CONSTRAINT fk_app_candidate FOREIGN KEY (candidate_id) REFERENCES users(id)
);

CREATE INDEX idx_job_id ON applications(job_id);
CREATE INDEX idx_candidate_id ON applications(candidate_id);