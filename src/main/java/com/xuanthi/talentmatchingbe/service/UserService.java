package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import com.xuanthi.talentmatchingbe.dto.user.UserUpdateRequest;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.mapper.UserMapper;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý các thao tác liên quan đến user profile
 * - Lấy thông tin profile
 * - Cập nhật thông tin cá nhân
 * - Quản lý avatar
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Lấy thông tin profile của user hiện tại
     *
     * @return UserResponse chứa thông tin profile
     * @throws RuntimeException nếu user không được xác thực hoặc không tồn tại
     */
    @Transactional(readOnly = true)
    public UserResponse getMyProfile() {
        // Lấy thông tin authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            log.error("Không thể xác thực người dùng hiện tại");
            throw new RuntimeException("Vui lòng đăng nhập để thực hiện thao tác này!");
        }

        String email = authentication.getName();
        log.debug("Lấy profile cho user: {}", email);

        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy user với email: {}", email);
                    return new RuntimeException("Không tìm thấy thông tin người dùng!");
                });

        log.info("Lấy profile thành công cho user: {}", email);
        return userMapper.toUserResponse(user);
    }

    /**
     * Cập nhật thông tin profile của user hiện tại
     *
     * @param request thông tin cần cập nhật
     * @return UserResponse chứa thông tin đã cập nhật
     * @throws RuntimeException nếu user không được xác thực hoặc không tồn tại
     * @throws IllegalArgumentException nếu request null
     */
    @Transactional
    public UserResponse updateProfile(UserUpdateRequest request) {
        // Validation đầu vào
        if (request == null) {
            log.error("UserUpdateRequest không được null");
            throw new IllegalArgumentException("Thông tin cập nhật không được rỗng!");
        }

        // Lấy thông tin authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            log.error("Không thể xác thực người dùng hiện tại");
            throw new RuntimeException("Vui lòng đăng nhập để thực hiện thao tác này!");
        }

        String email = authentication.getName();
        log.info("Cập nhật profile cho user: {}", email);

        // Tìm User trong DB
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy user với email: {}", email);
                    return new RuntimeException("Không tìm thấy thông tin người dùng!");
                });

        // MapStruct tự động copy dữ liệu từ request sang user
        userMapper.updateUserDetails(request, user);

        // Lưu lại vào database
        User updatedUser = userRepository.save(user);

        log.info("Cập nhật profile thành công cho user: {}", email);
        return userMapper.toUserResponse(updatedUser);
    }

    public String getOriginalCvUrl(Long candidateId) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ứng viên này!"));

        // Check xem ứng viên có up CV chưa (nhiều người tạo acc nhưng lười up CV)
        if (candidate.getCvUrl() == null || candidate.getCvUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Ứng viên này chưa cập nhật CV gốc dạng file!");
        }

        return candidate.getCvUrl();
    }
}