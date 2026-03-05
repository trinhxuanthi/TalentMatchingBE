package com.xuanthi.talentmatchingbe.dto.user;

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
    private Role role;
}
