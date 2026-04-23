package com.xuanthi.talentmatchingbe.entity;

import com.xuanthi.talentmatchingbe.enums.ApplicationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications", indexes = {
        @Index(name = "idx_app_job", columnList = "job_id"),
        @Index(name = "idx_app_candidate", columnList = "candidate_id"),
        @Index(name = "idx_app_score", columnList = "match_score") // Index cực kỳ quan trọng để HR xếp hạng CV
},
        uniqueConstraints = {
                // Ép DB chặn đứng 1 Email nộp 2 lần cho 1 Job
                @UniqueConstraint(name = "uk_job_email", columnNames = {"job_id", "email"})
        }
)
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
    @NotBlank(message = "CV URL không được để trống")
    private String cvUrl;

    @Column(name = "full_name", nullable = false)
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được quá 100 ký tự")
    private String fullName;

    @Column(nullable = false)
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Số điện thoại phải là 10-15 chữ số")
    private String phone;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Cover letter không được quá 2000 ký tự")
    private String coverLetter;

    // ==========================================
    // 2. BỘ LỌC CỨNG (Dành cho Java SQL WHERE)
    // ==========================================
    @Column(name = "education_level")
    @Size(max = 50, message = "Trình độ học vấn không được quá 50 ký tự")
    private String educationLevel; // VD: "Đại học", "Cao đẳng"

    @Column(name = "years_of_experience")
    @Min(value = 0, message = "Số năm kinh nghiệm phải >= 0")
    @Max(value = 50, message = "Số năm kinh nghiệm phải <= 50")
    private Integer yearsOfExperience; // VD: 3 (Lưu số nguyên để query >= 3)

    // ==========================================
    // 3. KẾT QUẢ TỪ LÕI AI PYTHON
    // ==========================================
    @Column(name = "match_score")
    @Min(value = 0, message = "Điểm số phải >= 0")
    @Max(value = 100, message = "Điểm số phải <= 100")
    private Integer matchScore;

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
    @Size(max = 1000, message = "Ghi chú không được quá 1000 ký tự")
    private String notes; // HR ghi chú riêng (VD: "Hẹn phỏng vấn thứ 2")

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true; // Soft delete

    // ==========================================
    // 5. LIÊN KẾT & TIMESTAMPS
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @NotNull(message = "Job không được null")
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    @NotNull(message = "Candidate không được null")
    private User candidate;

    @CreationTimestamp
    @Column(name = "applied_at", updatable = false)
    private LocalDateTime appliedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "ai_recommendation", length = 255)
    @Size(max = 255, message = "AI recommendation không được quá 255 ký tự")
    private String aiRecommendation;
}