package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.admin.AdminUserDetailResponse;
import com.xuanthi.talentmatchingbe.dto.admin.AdminUserResponse;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.Role;
import com.xuanthi.talentmatchingbe.repository.ApplicationRepository;
import com.xuanthi.talentmatchingbe.repository.JobRepository;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lấy danh sách User (Có phân trang)
     */
    public Page<AdminUserResponse> getAllUsers(String keyword, Role role, Boolean isActive, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Sếp có thể thay findAll bằng query lọc theo keyword nếu đã viết trong Repo
        Page<User> users = userRepository.findAll(pageable);

        return users.map(user -> AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build());
    }

    /**
     * Lấy chi tiết User + Thống kê thực tế từ Database
     */
    public AdminUserDetailResponse getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + id));

        // 🔥 LOGIC THỐNG KÊ THỰC TẾ:
        int appliedCount = 0;
        int postedCount = 0;

        if (user.getRole() == Role.CANDIDATE) {
            // Ép kiểu (int) để đồng bộ với DTO
            appliedCount = (int) applicationRepository.countByCandidateId(user.getId());
        } else if (user.getRole() == Role.EMPLOYER) {
            // Ép kiểu (int) để đồng bộ với DTO
            postedCount = (int) jobRepository.countByEmployerId(user.getId());
        }

        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhoneNumber())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .isActive(user.isActive())
                .totalAppliedJobs(appliedCount) // Đã có dữ liệu thật
                .totalPostedJobs(postedCount)   // Đã có dữ liệu thật
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Khóa/Mở khóa (Chặn Admin đụng vào Admin)
     */
    @Transactional
    public void toggleUserStatus(Long id, boolean isActive, String reason) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));

        // 🚨 GIÁP BẢO VỆ: Không cho phép Admin thao tác trên tài khoản Admin khác
        if (targetUser.getRole() == Role.ADMIN) {
            throw new RuntimeException("Lỗi bảo mật: Không thể thay đổi trạng thái của tài khoản Quản trị viên!");
        }

        targetUser.setActive(isActive);
        userRepository.save(targetUser);
    }

    /**
     * Đổi Role (Chặn Admin đổi quyền của Admin)
     */
    @Transactional
    public void changeUserRole(Long id, Role newRole) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));

        // 🚨 GIÁP BẢO VỆ: Không cho phép đổi Role của Admin hoặc đổi ai đó thành Admin bừa bãi
        if (targetUser.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không thể thay đổi quyền hạn của tài khoản Quản trị viên!");
        }

        targetUser.setRole(newRole);
        userRepository.save(targetUser);
        log.info("Admin changed role of user {} to {}", targetUser.getEmail(), newRole);
    }

    @Transactional
    public void emergencyResetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}