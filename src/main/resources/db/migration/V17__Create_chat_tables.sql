-- 1. Bảng Phòng Chat (Conversation)
CREATE TABLE conversations (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               employer_id BIGINT NOT NULL,   -- ID của HR
                               candidate_id BIGINT NOT NULL,  -- ID của Ứng viên
                               job_id BIGINT,                 -- (Tùy chọn) Chat về công việc nào
                               last_message VARCHAR(500),     -- Lưu tin nhắn cuối cùng để hiển thị ở danh sách (giống Messenger)
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Thời gian có tin nhắn mới nhất

                               CONSTRAINT fk_conv_employer FOREIGN KEY (employer_id) REFERENCES users(id) ON DELETE CASCADE,
                               CONSTRAINT fk_conv_candidate FOREIGN KEY (candidate_id) REFERENCES users(id) ON DELETE CASCADE,
                               CONSTRAINT fk_conv_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE SET NULL,

    -- Đảm bảo 1 HR và 1 Candidate chỉ có 1 phòng chat duy nhất cho 1 Job
                               CONSTRAINT uk_conversation UNIQUE (employer_id, candidate_id, job_id)
);

-- 2. Bảng Tin Nhắn (Messages)
CREATE TABLE messages (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          conversation_id BIGINT NOT NULL,
                          sender_id BIGINT NOT NULL,     -- ID người gửi (có thể là HR hoặc Candidate)
                          content TEXT NOT NULL,         -- Nội dung tin nhắn
                          is_read BOOLEAN DEFAULT FALSE, -- Trạng thái Đã đọc / Chưa đọc
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_msg_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
                          CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);