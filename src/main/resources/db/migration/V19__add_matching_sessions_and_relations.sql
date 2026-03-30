-- 1. TẠO BẢNG CHA (MATCHING SESSIONS)
CREATE TABLE matching_sessions (
                                   session_id VARCHAR(100) PRIMARY KEY,
                                   jd_file_name VARCHAR(255),
                                   jd_text LONGTEXT,
                                   custom_rules LONGTEXT,
                                   total_cvs INT,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 🔥 LƯU Ý DỌN RÁC (Xóa data test cũ để tránh lỗi Khóa ngoại):
TRUNCATE TABLE candidate_rankings;

-- 2. NỐI KHÓA NGOẠI CHO BẢNG CŨ (CANDIDATE RANKINGS)
ALTER TABLE candidate_rankings
    ADD CONSTRAINT fk_session
        FOREIGN KEY (session_id) REFERENCES matching_sessions(session_id) ON DELETE CASCADE;
