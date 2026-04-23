package com.xuanthi.talentmatchingbe.dto.job;

import com.xuanthi.talentmatchingbe.enums.EducationLevel;
import com.xuanthi.talentmatchingbe.enums.JobLevel;
import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.enums.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private Long id;

    // ==========================================
    // 1. KHỐI THÔNG TIN CƠ BẢN & LƯƠNG
    // ==========================================
    private String title;
    private String location;

    private boolean isSalaryNegotiable; // Cờ Lương thỏa thuận
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;

    private LocalDateTime deadline;
    private boolean isExpired;

    // ==========================================
    // 2. KHỐI THÔNG TIN SIDEBAR (Lọc & Phân loại)
    // ==========================================
    private JobType jobType;
    private String experienceLevel;

    // 🔥 THÊM MỚI: Trả về số năm kinh nghiệm tối thiểu
    private Integer minExpYears;

    // 🔥 CẬP NHẬT: Ép kiểu Enum
    private JobLevel jobLevel;

    // 🔥 CẬP NHẬT: Ép kiểu Enum
    private EducationLevel educationLevel;

    private Integer quantity;

    // ==========================================
    // 3. KHỐI VĂN BẢN CHI TIẾT
    // ==========================================
    private String description;
    private String benefits;
    private String requirements;

    // ==========================================
    // 4. KHỐI KỸ NĂNG CỐT LÕI & DANH MỤC
    // ==========================================
    // 🔥 CẬP NHẬT: Gộp thành 1 List duy nhất, Frontend sẽ vẽ Tag đồng loạt
    private List<String> requiredSkills;
    private List<String> categories;

    // ==========================================
    // 5. QUẢN LÝ TRẠNG THÁI & EMPLOYER
    // ==========================================
    private JobStatus status;
    private LocalDateTime createdAt;

    // Thông tin Employer để Frontend hiển thị Card hoặc làm tính năng Chat
    private Long employerId;
    private String employerName;
    private String employerAvatar;
    private String employerEmail;
    private AiWeightDto aiSettings;
    private boolean isPriority;
}