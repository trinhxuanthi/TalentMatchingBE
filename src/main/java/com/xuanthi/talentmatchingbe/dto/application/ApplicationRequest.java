package com.xuanthi.talentmatchingbe.dto.application;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationRequest {

    // ==========================================
    // 1. THÔNG TIN CƠ BẢNs
    // ==========================================
    @NotNull(message = "ID công việc không được để trống")
    private Long jobId;

    @NotBlank(message = "Vui lòng cung cấp đường dẫn CV")
    private String cvUrl;

    private String coverLetter; // Thư giới thiệu (Có thể bỏ trống)

    // ==========================================
    // 2. THÔNG TIN CÁ NHÂN (Nhập từ Form)
    // ==========================================
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    // ==========================================
    // 3. BỘ LỌC CỨNG (Chọn từ Dropdown)
    // ==========================================
    @NotBlank(message = "Vui lòng chọn trình độ học vấn")
    private String educationLevel;

    @NotNull(message = "Vui lòng nhập số năm kinh nghiệm")
    @Min(value = 0, message = "Số năm kinh nghiệm không được nhỏ hơn 0")
    private Integer yearsOfExperience;
}