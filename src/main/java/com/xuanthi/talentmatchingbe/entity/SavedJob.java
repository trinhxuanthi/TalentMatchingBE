package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho mối quan hệ lưu công việc của ứng viên
 * - Mỗi ứng viên chỉ có thể lưu một công việc một lần
 * - Theo dõi thời gian lưu và cập nhật
 */
@Entity
@Table(name = "saved_jobs",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"candidate_id", "job_id"})
       },
       indexes = {
           @Index(name = "idx_saved_jobs_candidate", columnList = "candidate_id"),
           @Index(name = "idx_saved_jobs_job", columnList = "job_id"),
           @Index(name = "idx_saved_jobs_saved_at", columnList = "saved_at")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedJob {

    /**
     * ID duy nhất của bản ghi saved job
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ứng viên đã lưu công việc này
     * Không được null, fetch lazy để tối ưu performance
     */
    @NotNull(message = "Candidate không được null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    /**
     * Công việc được lưu
     * Không được null, fetch lazy để tối ưu performance
     */
    @NotNull(message = "Job không được null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    /**
     * Thời gian lưu công việc
     * Tự động tạo khi insert, không được update
     */
    @CreationTimestamp
    @Column(name = "saved_at", updatable = false)
    private LocalDateTime savedAt;

}