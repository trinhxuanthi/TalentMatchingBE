-- V33: Xóa cột is_viewed thừa thãi do đã chuyển sang dùng ApplicationStatus Enum
ALTER TABLE applications DROP COLUMN is_viewed;