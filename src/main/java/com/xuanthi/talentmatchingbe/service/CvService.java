package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CvService {
    private final UserRepository userRepository;

    @Transactional
    public void updateCvUrl(String email, String cvUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setCvUrl(cvUrl);
        user.setIsCvAnalyzed(false); // Reset lại để AI quét lại từ đầu nếu họ đổi CV
        userRepository.save(user);
    }
}
