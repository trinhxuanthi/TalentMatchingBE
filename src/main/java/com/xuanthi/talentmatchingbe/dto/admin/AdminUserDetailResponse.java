package com.xuanthi.talentmatchingbe.dto.admin;

import com.xuanthi.talentmatchingbe.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String avatar;
    private Role role;
    private Boolean isActive;

    // Admin có quyền xem ghi chú lúc bị ban (nếu có)
    private String banReason;

    // === CÁC CHỈ SỐ THỐNG KÊ (Dành cho chức năng quản trị) ===
    private Integer totalAppliedJobs; // Nếu là CANDIDATE: Số job đã nộp
    private Integer totalPostedJobs;  // Nếu là EMPLOYER: Số job đã đăng

    private LocalDateTime lastLoginAt; // Xem nó có đang dùng app không hay nick ảo
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}