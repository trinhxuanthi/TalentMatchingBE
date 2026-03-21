-- Thêm cột logo_url để lưu ảnh đại diện/logo của công ty
ALTER TABLE companies
    ADD COLUMN logo_url VARCHAR(500) AFTER website;

-- Thêm cột description để lưu bài viết giới thiệu công ty (TopCV style)
-- Dùng kiểu TEXT để HR có thể viết bài dài, dùng các thẻ HTML (xuống dòng, in đậm...)
ALTER TABLE companies
    ADD COLUMN description TEXT AFTER logo_url;