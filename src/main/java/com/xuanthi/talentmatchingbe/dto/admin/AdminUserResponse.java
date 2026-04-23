package com.xuanthi.talentmatchingbe.dto.admin;

import com.xuanthi.talentmatchingbe.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private Boolean isActive;

    // Format ngày tháng để Admin biết thằng này lập nick từ bao giờ
    private LocalDateTime createdAt;
}
