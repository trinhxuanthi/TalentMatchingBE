package com.xuanthi.talentmatchingbe.entity;

import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.enums.JobType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    // 2. KHỐI THÔNG TIN SIDEBAR (Chuẩn TopCV)
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 50)
    private JobType jobType; // FULL_TIME, PART_TIME, REMOTE...

    @Column(name = "experience_level")
    private String experienceLevel; // VD: "Trên 5 năm kinh nghiệm"

    @Column(name = "job_level")
    private String jobLevel; // VD: "Quản lý / Giám sát", "Nhân viên"

    @Column(name = "education_level")
    private String educationLevel; // VD: "Đại học trở lên"

    @Column(name = "quantity")
    private Integer quantity; // Số lượng tuyển: VD 2

    // ==========================================
    // 3. KHỐI VĂN BẢN CHI TIẾT (Hiển thị UI)
    // ==========================================
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description; // Mô tả công việc chung

    @Column(columnDefinition = "TEXT")
    private String benefits; // Quyền lợi ứng viên

    // ==========================================
    // 4. KHỐI DỮ LIỆU DÀNH CHO LÕI AI PYTHON
    // ==========================================
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements; // Dành cho SBERT đọc (Mô tả chi tiết kinh nghiệm yêu cầu)

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills; // Lưu JSON trọng số kỹ năng. VD: {"java": 3, "mysql": 1}

    @Column(name = "categories", columnDefinition = "TEXT")
    private String categories; // Lưu mảng JSON danh mục. VD: ["IT - Phần mềm", "System Architect"]

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