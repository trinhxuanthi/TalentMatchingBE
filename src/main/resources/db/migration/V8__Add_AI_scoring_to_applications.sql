
-- Thêm cột lưu điểm phần trăm của AI (từ 0.00 đến 100.00)
ALTER TABLE applications
    ADD COLUMN match_score DECIMAL(5,2) DEFAULT NULL;

-- Thêm cột đánh dấu AI đã chấm điểm hay chưa (0 = chưa chấm, 1 = đã chấm)
ALTER TABLE applications
    ADD COLUMN is_ai_scored BOOLEAN DEFAULT FALSE;