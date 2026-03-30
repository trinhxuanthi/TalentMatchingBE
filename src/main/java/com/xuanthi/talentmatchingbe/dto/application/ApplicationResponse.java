package com.xuanthi.talentmatchingbe.dto.application;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.xuanthi.talentmatchingbe.enums.ApplicationStatus;
import com.xuanthi.talentmatchingbe.enums.EducationLevel;
import com.xuanthi.talentmatchingbe.enums.JobLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    // ==========================================
    // 1. THÔNG TIN ĐỊNH DANH
    // ==========================================
    private Long id; // ID của đơn ứng tuyển
    private Long jobId;
    private String jobTitle;
    private Long candidateId; // Có thể null nếu ứng viên nộp dạng khách (Guest)

    // ==========================================
    // 2. THÔNG TIN CÁ NHÂN & CV
    // ==========================================
    private String candidateFullName;
    private String candidateEmail;
    private String phone; // 🔥 Bổ sung SĐT để HR bốc máy gọi luôn

    private String cvUrl;
    private String coverLetter;

    // ==========================================
    // 3. THÔNG TIN BỘ LỌC CỨNG (Ứng viên khai)
    // ==========================================
    private EducationLevel educationLevel; // Ép kiểu Enum
    private JobLevel jobLevel;             // Ép kiểu Enum
    private Integer yearsOfExperience;
    private List<String> coreSkills;       // VD: ["Java", "Spring Boot"] -> Frontend vẽ Tag xanh đỏ

    // ==========================================
    // 4. KẾT QUẢ ĐÁNH GIÁ TỪ HỆ THỐNG AI
    // ==========================================
    private Integer matchScore;         // 🔥 Điểm AI chấm (Ví dụ: 85 điểm)
    private String aiRecommendation;    // 🔥 Lời khuyên của AI (VD: "TIẾN HÀNH PHỎNG VẤN")

    // ==========================================
    // 5. TRẠNG THÁI & GHI CHÚ CỦA HR
    // ==========================================
    private ApplicationStatus status; // PENDING, REVIEWING, INTERVIEW, REJECTED, HIRED
    private String notes;             // Ghi chú nội bộ của HR (VD: "Ứng viên này đòi lương hơi cao")

    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    @JsonRawValue
    private String aiAnalysis;

}