-- ==========================================
-- Tắt kiểm tra khóa ngoại để xóa bảng an toàn (Đề phòng có bảng khác đang móc vào jobs)
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa hẳn bảng cũ bị sai cấu trúc đi
DROP TABLE IF EXISTS jobs;

-- Bật lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;
-- TẠO BẢNG JOBS (TIN TUYỂN DỤNG)
-- ==========================================
CREATE TABLE jobs (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 1. Khối thông tin cơ bản
                      title VARCHAR(255) NOT NULL,
                      location VARCHAR(255) NOT NULL,
                      salary_min DECIMAL(19, 2),
                      salary_max DECIMAL(19, 2),
                      is_salary_negotiable BOOLEAN DEFAULT FALSE,
                      deadline DATETIME,

    -- 2. Khối thông tin phân loại (Enums & UI)
                      job_type VARCHAR(50),               -- Lưu Enum (FULL_TIME, PART_TIME...)
                      experience_level VARCHAR(255),      -- Text hiện UI (VD: "2 - 3 năm")
                      min_exp_years INT NOT NULL DEFAULT 0, -- Dùng cho "Máy Chém" Java
                      job_level VARCHAR(50),              -- Lưu Enum (FRESHER, MIDDLE...)
                      education_level VARCHAR(50),        -- Lưu Enum (BACHELOR, MASTER...)
                      quantity INT,

    -- 3. Khối văn bản chi tiết (Dùng LONGTEXT cho MySQL để chứa nội dung an toàn)
                      description LONGTEXT NOT NULL,
                      benefits LONGTEXT,
                      requirements LONGTEXT,

    -- 4. Khối điều kiện cứng (Lưu List<String> dưới dạng chuỗi cách nhau bằng dấu phẩy)
                      required_skills LONGTEXT,
                      categories LONGTEXT,

    -- 5. Quản lý trạng thái & Liên kết
                      status VARCHAR(20) DEFAULT 'OPEN',
                      is_active BOOLEAN DEFAULT TRUE,

    -- Liên kết với bảng users (employer). Giả định bro đã có bảng users trước đó.
                      employer_id BIGINT NOT NULL,

                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Ràng buộc khóa ngoại (Móc vào bảng users của bro)
                      CONSTRAINT fk_job_employer FOREIGN KEY (employer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==========================================
-- ĐÁNH INDEX TỐI ƯU TỐC ĐỘ TÌM KIẾM
-- ==========================================
-- Giúp API tìm kiếm việc làm (Search) chạy nhanh hơn khi query theo Title, Location, hoặc Status
CREATE INDEX idx_job_title ON jobs(title);
CREATE INDEX idx_job_location ON jobs(location);
CREATE INDEX idx_job_status ON jobs(status);