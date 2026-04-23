-- V27: Tạo bảng trung gian để lưu trạng thái Theo dõi công ty của Ứng viên
CREATE TABLE company_follows (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 company_id BIGINT NOT NULL,

    -- Chống một người follow 1 công ty 2 lần
                                 CONSTRAINT uk_user_company UNIQUE (user_id, company_id),

    -- Khóa ngoại liên kết với bảng users và companies
    -- (Sếp kiểm tra lại xem bảng user của sếp tên là 'users' hay 'user' để sửa lại cho đúng nhé, thường là 'users')
                                 CONSTRAINT fk_cf_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_cf_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);