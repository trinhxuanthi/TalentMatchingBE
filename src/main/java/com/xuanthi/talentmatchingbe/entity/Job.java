package com.xuanthi.talentmatchingbe.entity;

import com.xuanthi.talentmatchingbe.converter.StringListConverter;
import com.xuanthi.talentmatchingbe.enums.EducationLevel;
import com.xuanthi.talentmatchingbe.enums.JobLevel;
import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.enums.JobType;
import jakarta.persistence.*;
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
    private String title;

    @Column(nullable = false)
    private String location;

    @Column(name = "salary_min", precision = 19, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 19, scale = 2)
    private BigDecimal salaryMax;

    @Builder.Default
    @Column(name = "is_salary_negotiable")
    private boolean isSalaryNegotiable = false;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    // ==========================================
    // 2. KHỐI THÔNG TIN SIDEBAR (Chuẩn UI & Logic)
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 50)
    private JobType jobType;

    // Cột để hiện text lên UI (VD: "2 - 3 năm kinh nghiệm")
    @Column(name = "experience_level")
    private String experienceLevel;

    // 🔥 Cột ẨN để chạy "Máy chém" Java Logic (VD: 2)
    @Builder.Default
    @Column(name = "min_exp_years", nullable = false)
    private Integer minExpYears = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_level", length = 50)
    private JobLevel jobLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", length = 50)
    private EducationLevel educationLevel;

    @Column(name = "quantity")
    private Integer quantity;

    // ==========================================
    // 3. KHỐI VĂN BẢN CHI TIẾT (Cho ứng viên & AI đọc)
    // ==========================================
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    // ==========================================
    // 4. KHỐI ĐIỀU KIỆN CỨNG (Cho Frontend Tag Input & Java Máy Chém)
    // ==========================================

    // 🔥 ẢO THUẬT JPA: Nhận List từ Frontend, lưu String xuống MySQL
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
    @Column(length = 20)
    private JobStatus status = JobStatus.OPEN;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    // Liên kết với nhà tuyển dụng (Giữ nguyên của bro)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}