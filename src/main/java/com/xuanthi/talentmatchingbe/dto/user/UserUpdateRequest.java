package com.xuanthi.talentmatchingbe.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO cho yêu cầu cập nhật thông tin user
 * Chứa các trường có thể cập nhật của user profile
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {

    /**
     * Họ và tên đầy đủ của user (bắt buộc, 2-100 ký tự)
     */
    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2-100 ký tự")
    private String fullName;

    /**
     * Số điện thoại (tùy chọn, phải có 10 chữ số nếu có)
     * Regex: (^$|[0-9]{10}) - cho phép empty hoặc đúng 10 số
     */
    @Pattern(regexp = "(^$|[0-9]{10})", message = "Số điện thoại phải có đúng 10 chữ số")
    private String phoneNumber;
}
