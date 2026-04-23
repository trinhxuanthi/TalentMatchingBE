-- 1. Tạo bảng ai_settings
CREATE TABLE IF NOT EXISTS `ai_settings` (
                                             `id` BIGINT NOT NULL,
                                             `filter_title_score` DOUBLE DEFAULT 65.0,
                                             `filter_total_score` DOUBLE DEFAULT 50.0,
                                             `sbert_rejection_threshold` DOUBLE DEFAULT 0.4,
                                             `sbert_weight_title` DOUBLE DEFAULT 0.45,
                                             `sbert_weight_skills` DOUBLE DEFAULT 0.40,
                                             `sbert_weight_exp` DOUBLE DEFAULT 0.15,
                                             PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Chèn dữ liệu mặc định
INSERT IGNORE INTO `ai_settings` (`id`, `filter_title_score`, `filter_total_score`, `sbert_rejection_threshold`, `sbert_weight_title`, `sbert_weight_skills`, `sbert_weight_exp`)
VALUES (1, 65.0, 50.0, 0.4, 0.45, 0.40, 0.15);

-- 2. Tạo bảng ai_job_settings
CREATE TABLE IF NOT EXISTS `ai_job_settings` (
                                                 `id` BIGINT NOT NULL AUTO_INCREMENT,
                                                 `weight_exp` DOUBLE DEFAULT 0.30,
                                                 `weight_skills` DOUBLE DEFAULT 0.30,
                                                 `weight_role` DOUBLE DEFAULT 0.15,
                                                 `weight_tools` DOUBLE DEFAULT 0.10,
                                                 `weight_edu` DOUBLE DEFAULT 0.10,
                                                 `weight_soft` DOUBLE DEFAULT 0.05,
                                                 PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Cập nhật bảng jobs (ĐÃ SỬA CÚ PHÁP LỖI Ở ĐÂY)
-- Lưu ý: Xóa bỏ "IF NOT EXISTS" vì MySQL 8.0 không hỗ trợ trong ALTER TABLE
ALTER TABLE `jobs`
    ADD COLUMN `ai_job_setting_id` BIGINT DEFAULT NULL;

ALTER TABLE `jobs`
    ADD CONSTRAINT `FK_JOB_AI_SETTING`
        FOREIGN KEY (`ai_job_setting_id`) REFERENCES `ai_job_settings` (`id`)
            ON DELETE SET NULL;