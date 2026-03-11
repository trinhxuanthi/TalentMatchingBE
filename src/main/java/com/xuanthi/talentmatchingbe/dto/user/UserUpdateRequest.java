package com.xuanthi.talentmatchingbe.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Pattern(regexp = "(^$|[0-9]{10})", message = "Số điện thoại phải có 10 chữ số")
    private String phoneNumber;
}
