package com.xuanthi.talentmatchingbe.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý upload file lên Cloudinary
 * - Upload avatar cho user
 * - Validation file type và size
 * - Error handling chi tiết
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UploadService {

    private final Cloudinary cloudinary;

    // File size limits
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB

    // Allowed image types
    private static final List<String> ALLOWED_AVATAR_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    /**
     * Upload avatar cho user
     * Tự động nén ảnh và đưa vào thư mục 'avatars' trên Cloudinary
     *
     * @param file MultipartFile chứa avatar (bắt buộc, không null)
     * @return URL của ảnh đã upload
     * @throws IllegalArgumentException nếu file không hợp lệ
     * @throws IOException nếu có lỗi khi upload
     */
    public String uploadAvatar(MultipartFile file) throws IOException {
        // ===== VALIDATION =====
        if (file == null || file.isEmpty()) {
            log.error("File avatar không được rỗng");
            throw new IllegalArgumentException("File avatar không được rỗng!");
        }

        // Check file size
        if (file.getSize() > MAX_AVATAR_SIZE) {
            log.error("File avatar quá lớn: {} bytes (max: {} bytes)", file.getSize(), MAX_AVATAR_SIZE);
            throw new IllegalArgumentException("Kích thước file avatar không được vượt quá 5MB!");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AVATAR_TYPES.contains(contentType.toLowerCase())) {
            log.error("Loại file không được hỗ trợ: {}", contentType);
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh (JPEG, PNG, GIF, WebP)!");
        }

        // Check file name
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            log.error("Tên file không hợp lệ");
            throw new IllegalArgumentException("Tên file không hợp lệ!");
        }

        log.info("Bắt đầu upload avatar: {} (size: {} bytes, type: {})",
                originalFilename, file.getSize(), contentType);

        try {
            // ===== UPLOAD TO CLOUDINARY =====
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "avatars",
                            "resource_type", "auto"
                    ));

            String imageUrl = uploadResult.get("url").toString();
            log.info("Upload avatar thành công: {}", imageUrl);

            return imageUrl;

        } catch (IOException e) {
            log.error("Lỗi upload avatar lên Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Không thể upload avatar. Vui lòng thử lại!", e);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi upload avatar: {}", e.getMessage(), e);
            throw new RuntimeException("Đã xảy ra lỗi khi upload avatar!", e);
        }
    }
}
