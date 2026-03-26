package com.xuanthi.talentmatchingbe.dto.job;

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

    private boolean isSalaryNegotiable; // Thêm cờ Lương thỏa thuận
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;

    private LocalDateTime deadline;
    private boolean isExpired;

    // ==========================================
    // 2. KHỐI THÔNG TIN SIDEBAR (Chuẩn TopCV)
    // ==========================================
    private JobType jobType;
    private String experienceLevel;
    private String jobLevel;
    private String educationLevel;
    private Integer quantity;

    // ==========================================
    // 3. KHỐI VĂN BẢN CHI TIẾT
    // ==========================================
    private String description;
    private String benefits;
    private String requirements; // Text yêu cầu công việc

    // ==========================================
    // 4. KHỐI KỸ NĂNG & DANH MỤC (Đã được parse từ JSON DB)
    // ==========================================
    private List<String> mustHaveSkills;   // Vẽ Tag màu Đỏ/Cam
    private List<String> niceToHaveSkills; // Vẽ Tag màu Xanh/Xám
    private List<String> categories;       // Vẽ Tag Danh mục nghề

    // ==========================================
    // 5. QUẢN LÝ TRẠNG THÁI & EMPLOYER
    // ==========================================
    private JobStatus status;
    private LocalDateTime createdAt;

    // TỐI ƯU: Trả về thông tin cơ bản của Employer để Frontend hiển thị luôn
    private Long employerId;
    private String employerName;
    private String employerAvatar;
    private String employerEmail;
}