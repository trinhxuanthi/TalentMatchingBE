package com.xuanthi.talentmatchingbe.entity;

import com.xuanthi.talentmatchingbe.converter.StringListConverter;
import com.xuanthi.talentmatchingbe.enums.EducationLevel;
import com.xuanthi.talentmatchingbe.enums.JobLevel;
import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.enums.JobType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_job_title", columnList = "title"),
        @Index(name = "idx_job_location", columnList = "location"),
        @Index(name = "idx_job_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // 1. KHỐI THÔNG TIN CƠ BẢN
    // ==========================================
    @Column(nullable = false)
    @NotBlank(message = "Tên công việc không được để trống")
    @Size(min = 5, max = 200, message = "Tên công việc phải từ 5-200 ký tự")
    private String title;

    @Column(nullable = false)
    @NotBlank(message = "Địa điểm làm việc không được để trống")
    @Size(max = 255, message = "Địa điểm không được quá 255 ký tự")
    private String location;

    @Column(name = "salary_min", precision = 19, scale = 2)
    @Min(value = 0, message = "Lương tối thiểu phải >= 0")
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 19, scale = 2)
    @Min(value = 0, message = "Lương tối đa phải >= 0")
    private BigDecimal salaryMax;

    @Builder.Default
    @Column(name = "is_salary_negotiable")
    private boolean isSalaryNegotiable = false;

    @Column(name = "deadline")
    @Future(message = "Hạn chót phải là ngày trong tương lai")
    private LocalDateTime deadline;

    // ==========================================
    // 2. KHỐI THÔNG TIN SIDEBAR (Chuẩn UI & Logic)
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 50)
    @NotNull(message = "Loại công việc không được null")
    private JobType jobType;

    @Column(name = "experience_level")
    @Size(max = 255, message = "Mức kinh nghiệm không được quá 255 ký tự")
    private String experienceLevel;

    @Builder.Default
    @Column(name = "min_exp_years", nullable = false)
    @Min(value = 0, message = "Số năm kinh nghiệm phải >= 0")
    @Max(value = 50, message = "Số năm kinh nghiệm phải <= 50")
    private Integer minExpYears = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_level", length = 50)
    private JobLevel jobLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", length = 50)
    private EducationLevel educationLevel;

    @Column(name = "quantity")
    @Min(value = 1, message = "Số lượng vị trí phải >= 1")
    private Integer quantity;

    // ==========================================
    // 3. KHỐI VĂN BẢN CHI TIẾT (Cho ứng viên & AI đọc)
    // ==========================================
    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Mô tả công việc không được để trống")
    @Size(min = 20, max = 5000, message = "Mô tả phải từ 20-5000 ký tự")
    private String description;

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000, message = "Phúc lợi không được quá 2000 ký tự")
    private String benefits;

    @Column(name = "requirements", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Yêu cầu không được quá 2000 ký tự")
    private String requirements;

    // ==========================================
    // 4. KHỐI ĐIỀU KIỆN CỨNG (Cho Frontend Tag Input & Java Máy Chém)
    // ==========================================
    @Convert(converter = StringListConverter.class)
    @Column(name = "required_skills", columnDefinition = "TEXT")
    private List<String> requiredSkills;

    @Convert(converter = StringListConverter.class)
    @Column(name = "categories", columnDefinition = "TEXT")
    private List<String> categories;

    // ==========================================
    // 5. QUẢN LÝ TRẠNG THÁI & LIÊN KẾT
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 20, nullable = false)
    private JobStatus status = JobStatus.OPEN;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    @NotNull(message = "Nhà tuyển dụng không được null")
    private User employer;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_job_setting_id", referencedColumnName = "id") // ✅ Khớp với SQL V25
    private AiJobSetting aiJobSetting;

    // Nằm trong Job.java
    @Column(name = "is_priority")
    private boolean isPriority = false;
}