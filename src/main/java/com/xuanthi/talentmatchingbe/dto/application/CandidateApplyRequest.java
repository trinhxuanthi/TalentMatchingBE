package com.xuanthi.talentmatchingbe.dto.application;

import com.xuanthi.talentmatchingbe.enums.EducationLevel;
import com.xuanthi.talentmatchingbe.enums.JobLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CandidateApplyRequest {

    // ==========================================
    // 1. THÔNG TIN CƠ BẢN
    // ==========================================
    @NotNull(message = "ID công việc không được để trống")
    private Long jobId;

    @NotBlank(message = "Vui lòng cung cấp đường dẫn CV")
    private String cvUrl; // Frontend upload lên Cloudinary rồi truyền link xuống đây

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
    // 3. BỘ LỌC CỨNG (Dành cho Java Rule Engine)
    // ==========================================
    @NotNull(message = "Vui lòng chọn trình độ học vấn")
    private EducationLevel educationLevel; // 🔥 Đã ép kiểu Enum

    @NotNull(message = "Vui lòng chọn cấp bậc hiện tại của bạn")
    private JobLevel jobLevel; // 🔥 Thêm Enum để so sánh (VD: Fresher, Middle...)

    @NotNull(message = "Vui lòng nhập số năm kinh nghiệm")
    @Min(value = 0, message = "Số năm kinh nghiệm không được nhỏ hơn 0")
    private Integer yearsOfExperience;

    @NotEmpty(message = "Vui lòng chọn ít nhất 1 kỹ năng thế mạnh của bạn")
    private List<String> coreSkills; // 🔥 Mảng kỹ năng để Java đếm (VD: ["Java", "SQL"])
}