package com.xuanthi.talentmatchingbe.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.util.SecurityUtils; // Nhớ import class này của bro
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CvService {

    private final UserRepository userRepository;
    private final Cloudinary cloudinary; // Bơm "vũ khí" Cloudinary vào đây

    @Transactional
    public String uploadAndUpdateCv(MultipartFile file) {
        // 1. Lấy thông tin người dùng đang đăng nhập từ Token
        User currentUser = SecurityUtils.getCurrentUser();
        String email = currentUser.getEmail();

        try {
            // 2. Bắn file vật lý lên mây (Cloudinary)
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "auto", // Tự động nhận diện PDF/Word
                    "folder", "talent_matching/cv" // Gom vào 1 thư mục cho gọn
            ));
            String cvUrl = uploadResult.get("secure_url").toString();

            // 3. Cập nhật URL vào Database
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            user.setCvUrl(cvUrl);
            user.setIsCvAnalyzed(false); // Reset cờ để chuẩn bị gọi AI quét lại
            userRepository.save(user);

            // 4. Trả về link URL cho Frontend hiển thị (nếu cần)
            return cvUrl;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đẩy CV lên mây: " + e.getMessage());
        }
    }
}