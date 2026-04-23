package com.xuanthi.talentmatchingbe.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.xuanthi.talentmatchingbe.dto.cv.CvGenerateReq;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.event.CvUpdatedEvent;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CvService {

    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    @Value("${cv.max-size:5242880}") // 5MB default
    private long maxFileSize;

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    @Value("${python.ai.url:http://localhost:5000/api/quick-match}")
    private String pythonAiUrl;

    // Tái sử dụng 1 biến RestTemplate để tiết kiệm RAM
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public String uploadAndUpdateCv(MultipartFile file) {
        User currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null) {
            log.warn("Unauthorized CV upload attempt");
            throw new RuntimeException("Vui lòng đăng nhập trước!");
        }

        String email = currentUser.getEmail();
        log.info("CV upload initiated for user: {}", email);

        // Validate file
        validateFile(file);

        try {
            // Upload file lên Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "auto",
                    "folder", "talent_matching/cv",
                    "public_id", "cv_" + currentUser.getId() + "_" + System.currentTimeMillis()
            ));
            String cvUrl = uploadResult.get("secure_url").toString();
            log.info("CV uploaded successfully to Cloudinary: {}", cvUrl);

            // Cập nhật URL vào Database
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("User not found for email: {}", email);
                        return new RuntimeException("Không tìm thấy người dùng");
                    });

            user.setCvUrl(cvUrl);
            user.setIsCvAnalyzed(false); // Reset cờ để chuẩn bị gọi AI
            user.setCvUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("User {} CV updated successfully", email);
            eventPublisher.publishEvent(new CvUpdatedEvent(currentUser.getId(), cvUrl));
            return cvUrl;


        } catch (IOException e) {
            log.error("Error uploading CV to Cloudinary for user {}: {}", email, e.getMessage());
            throw new RuntimeException("Lỗi khi đẩy CV lên mây: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during CV upload for user {}: {}", email, e.getMessage());
            throw new RuntimeException("Lỗi không mong muốn: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        // Check empty
        if (file == null || file.isEmpty()) {
            log.warn("Empty file upload attempt");
            throw new RuntimeException("File không được để trống!");
        }

        // Check size
        if (file.getSize() > maxFileSize) {
            log.warn("File size exceeds limit: {} bytes", file.getSize());
            throw new RuntimeException("Kích thước file không được vượt quá 5MB!");
        }

        // Check file type
        String mimeType = file.getContentType();
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            log.warn("Invalid file type: {}", mimeType);
            throw new RuntimeException("Chỉ chấp nhận file PDF hoặc Word (.docx, .doc)!");
        }

        // Check filename
        String filename = file.getOriginalFilename();
        if (filename == null || !hasAllowedExtension(filename)) {
            log.warn("Invalid file extension: {}", filename);
            throw new RuntimeException("Tên file không hợp lệ!");
        }

        log.debug("File validation passed: {}", filename);
    }

    private boolean hasAllowedExtension(String filename) {
        String[] allowedExtensions = {"pdf", "doc", "docx"};
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return Arrays.asList(allowedExtensions).contains(extension);
    }

    // Dùng CompletableFuture để không block luồng chính của Spring Boot
    public CompletableFuture<Object> generateCvAsync(CvGenerateReq request) {
        return CompletableFuture.supplyAsync(() -> {
            String targetUrl = pythonAiUrl.replace("/api/quick-match", "") + "/api/generate-cv";
            try {
                ResponseEntity<Object> response = restTemplate.postForEntity(targetUrl, request, Object.class);
                return response.getBody();
            } catch (Exception e) {
                throw new RuntimeException("Máy chủ AI đang bận, vui lòng thử lại sau!");
            }
        });
    }


}