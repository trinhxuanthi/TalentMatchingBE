CREATE TABLE jobs (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      title VARCHAR(255) NOT NULL,
                      description TEXT NOT NULL,         -- Đổi thành NOT NULL để tránh bài đăng trống
                      location VARCHAR(255) NOT NULL,
                      salary_min DECIMAL(19, 2),         -- TỐI ƯU: Tách dải lương để dễ lọc (Lương từ...)
                      salary_max DECIMAL(19, 2),         -- TỐI ƯU: Tách dải lương (Lương đến...)
                      job_type VARCHAR(50),              -- Ví dụ: FULL_TIME, PART_TIME, REMOTE
                      experience_level VARCHAR(50),      -- Ví dụ: JUNIOR, SENIOR, INTERN
                      status VARCHAR(20) DEFAULT 'OPEN',
                      employer_id BIGINT NOT NULL,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                      CONSTRAINT fk_job_employer FOREIGN KEY (employer_id) REFERENCES users(id),

                      INDEX idx_job_title (title),
                      INDEX idx_job_location (location),
                      INDEX idx_job_status (status)
);