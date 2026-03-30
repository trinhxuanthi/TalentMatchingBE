-- Thêm cột
ALTER TABLE applications
    ADD COLUMN ai_recommendation VARCHAR(255);

-- Ép kiểu cột
ALTER TABLE applications
    MODIFY COLUMN ai_analysis LONGTEXT;