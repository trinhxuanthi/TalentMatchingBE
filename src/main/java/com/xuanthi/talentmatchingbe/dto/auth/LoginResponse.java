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
}