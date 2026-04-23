package com.xuanthi.talentmatchingbe.dto.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.xuanthi.talentmatchingbe.enums.ApplicationStatus;
import com.xuanthi.talentmatchingbe.enums.EducationLevel;
import com.xuanthi.talentmatchingbe.enums.JobLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho response của application (đơn ứng tuyển)
 * Chứa thông tin đầy đủ về đơn ứng tuyển và kết quả AI matching
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    // ==========================================
    // 1. THÔNG TIN ĐỊNH DANH
    // ==========================================
    /**
     * ID duy nhất của đơn ứng tuyển
     */
    @Positive(message = "ID phải là số dương")
    private Long id;

    /**
     * ID của công việc được ứng tuyển
     */
    @Positive(message = "Job ID phải là số dương")
    private Long jobId;

    /**
     * Tiêu đề công việc
     */
    @Size(max = 200, message = "Tiêu đề công việc không được vượt quá 200 ký tự")
    private String jobTitle;

    /**
     * ID của ứng viên (có thể null nếu ứng viên nộp dạng khách)
     */
    @Positive(message = "Candidate ID phải là số dương")
    private Long candidateId;

    // ==========================================
    // 2. THÔNG TIN CÁ NHÂN & CV
    // ==========================================
    /**
     * Họ và tên đầy đủ của ứng viên
     */
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String candidateFullName;

    /**
     * Email của ứng viên
     */
    @Email(message = "Email không đúng định dạng")
    @Size(max = 191, message = "Email không được vượt quá 191 ký tự")
    private String candidateEmail;

    /**
     * Số điện thoại của ứng viên
     */
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Số điện thoại phải có 10-15 chữ số")
    private String phone;

    /**
     * URL của CV đã upload
     */
    @Pattern(regexp = "^https?://.*", message = "CV URL phải là URL hợp lệ")
    @Size(max = 500, message = "CV URL không được vượt quá 500 ký tự")
    private String cvUrl;

    /**
     * Thư giới thiệu của ứng viên
     */
    @Size(max = 2000, message = "Cover letter không được vượt quá 2000 ký tự")
    private String coverLetter;

    // ==========================================
    // 3. THÔNG TIN BỘ LỌC CỨNG (Ứng viên khai)
    // ==========================================
    /**
     * Trình độ học vấn của ứng viên
     */
    private EducationLevel educationLevel;

    /**
     * Cấp bậc công việc hiện tại
     */
    private JobLevel jobLevel;

    /**
     * Số năm kinh nghiệm
     */
    @Min(value = 0, message = "Số năm kinh nghiệm không được âm")
    @Max(value = 50, message = "Số năm kinh nghiệm không được vượt quá 50")
    private Integer yearsOfExperience;

    /**
     * Danh sách kỹ năng chính (VD: ["Java", "Spring Boot"])
     */
    private List<@Size(max = 50, message = "Tên kỹ năng không được vượt quá 50 ký tự") String> coreSkills;

    // ==========================================
    // 4. KẾT QUẢ ĐÁNH GIÁ TỪ HỆ THỐNG AI
    // ==========================================
    /**
     * Điểm số matching tổng thể (0-100)
     */
    @Min(value = 0, message = "Match score không được âm")
    @Max(value = 100, message = "Match score không được vượt quá 100")
    private Integer matchScore;

    /**
     * Khuyến nghị của AI (VD: "TIẾN HÀNH PHỎNG VẤN")
     */
    @Size(max = 100, message = "AI recommendation không được vượt quá 100 ký tự")
    private String aiRecommendation;

    // ==========================================
    // 5. TRẠNG THÁI & GHI CHÚ CỦA HR
    // ==========================================
    /**
     * Trạng thái hiện tại của đơn ứng tuyển
     */
    private ApplicationStatus status;

    /**
     * Ghi chú nội bộ của HR
     */
    @Size(max = 1000, message = "Notes không được vượt quá 1000 ký tự")
    private String notes;

    /**
     * Thời gian nộp đơn
     */
    private LocalDateTime appliedAt;

    /**
     * Thời gian cập nhật cuối cùng
     */
    private LocalDateTime updatedAt;

    /**
     * Phân tích chi tiết từ AI (JSON format)
     */
    @JsonRawValue
    private String aiAnalysis;

    // Trong ApplicationResponse.java
    @JsonProperty("isCandidatePro")
    private boolean isCandidatePro;

}