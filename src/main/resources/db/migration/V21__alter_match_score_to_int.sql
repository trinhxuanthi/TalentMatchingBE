-- Bước 1: Làm tròn các điểm số cũ (nếu đã có data test) để tránh lỗi mất mát dữ liệu (Data Truncation)
-- Ví dụ: 85.6 sẽ làm tròn thành 86, 85.4 thành 85.
UPDATE applications
SET match_score = ROUND(match_score)
WHERE match_score IS NOT NULL;

-- Bước 2: Chính thức ép kiểu cột match_score từ DOUBLE sang INT
ALTER TABLE applications
    MODIFY COLUMN match_score INT;