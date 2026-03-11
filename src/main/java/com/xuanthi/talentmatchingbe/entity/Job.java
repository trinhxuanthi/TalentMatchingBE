package com.xuanthi.talentmatchingbe.entity;

import com.xuanthi.talentmatchingbe.enums.JobStatus; // Nhớ tạo Enum này
import com.xuanthi.talentmatchingbe.enums.JobType;   // Nhớ tạo Enum này
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

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String location;

    // TỐI ƯU: Dùng BigDecimal để tính toán lương chính xác tuyệt đối
    @Column(name = "salary_min", precision = 19, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 19, scale = 2)
    private BigDecimal salaryMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 50)
    private JobType jobType; // FULL_TIME, PART_TIME, REMOTE...

    @Column(name = "experience_level")
    private String experienceLevel; // INTERN, JUNIOR, SENIOR...

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 20)
    private JobStatus status = JobStatus.OPEN;

    // TỐI ƯU: Lazy loading để không load User khi không cần thiết
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ... các trường cũ giữ nguyên ...

    @Column(name = "deadline")
    private LocalDateTime deadline; // Quan trọng: Để tự động ẩn Job khi hết hạn

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements; // Tách riêng yêu cầu để sau này Python AI soi cho chuẩn

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true; // Dùng để ẩn bài đăng nhanh (Soft Delete)
}