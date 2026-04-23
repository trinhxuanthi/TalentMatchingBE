package com.xuanthi.talentmatchingbe.dto.auth;

import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private UserResponse user;
    private String urlAvatarCompany;
    private Long idCompany; // Thêm thông tin công ty nếu người dùng là employer
}