package com.xuanthi.talentmatchingbe.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xuanthi.talentmatchingbe.enums.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String avatar;
    private Role role;
    private String phoneNumber;
    private String provider;
    private String cvUrl;
    @JsonProperty("isActive")
    private boolean isActive;
}
