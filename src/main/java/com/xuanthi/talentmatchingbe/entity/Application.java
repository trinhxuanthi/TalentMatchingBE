package com.xuanthi.talentmatchingbe.entity;

import com.xuanthi.talentmatchingbe.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications", indexes = {
        @Index(name = "idx_app_job", columnList = "job_id"),
        @Index(name = "idx_app_candidate", columnList = "candidate_id"),
        @Index(name = "idx_app_score", columnList = "match_score") // Index cực kỳ quan trọng để HR xếp hạng CV
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // 1. DỮ LIỆU TỪ FORM ỨNG TUYỂN (Thông tin liên hệ & CV)
    // ==========================================
    @Column(columnDefinition = "TEXT", nullable = false)
    private String cvUrl;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    // ==========================================
    // 2. BỘ LỌC CỨNG (Dành cho Java SQL WHERE)
    // ==========================================
    @Column(name = "education_level")
    private String educationLevel; // VD: "Đại học", "Cao đẳng"

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience; // VD: 3 (Lưu số nguyên để query >= 3)

    // ==========================================
    // 3. KẾT QUẢ TỪ LÕI AI PYTHON
    // ==========================================
    @Column(name = "match_score", columnDefinition = "DECIMAL(5,2)")
    private Double matchScore; // Điểm AI (0.00 - 100.00)

    // Cờ trạng thái gọi AI: 0 (Chưa gọi/Đang chờ), 1 (Đã có điểm), -1 (AI lỗi/Không đọc được CV)
    @Column(name = "is_ai_scored")
    @Builder.Default
    private Integer isAiScored = 0;

    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis; // Phân tích kỹ năng điểm mạnh/yếu (JSON)

    // ==========================================
    // 4. QUẢN LÝ TRẠNG THÁI & HR TRACKING (Từ code cũ của bạn)
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes; // HR ghi chú riêng (VD: "Hẹn phỏng vấn thứ 2")

    @Builder.Default
    @Column(name = "is_viewed")
    private boolean isViewed = false; // HR đã mở xem CV chưa

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true; // Soft delete

    // ==========================================
    // 5. LIÊN KẾT & TIMESTAMPS
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @CreationTimestamp
    @Column(name = "applied_at", updatable = false)
    private LocalDateTime appliedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}