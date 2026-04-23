package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nối với bảng User (Ứng viên)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    // Nối với bảng Job
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // Điểm tổng
    @Column(name = "match_score", nullable = false)
    private Double matchScore;

    // Điểm chi tiết (Breakdown)
    @Column(name = "skill_score")
    private Double skillScore;

    @Column(name = "title_score")
    private Double titleScore;

    @Column(name = "exp_score")
    private Double expScore;

    // 🚨 Cờ hiệu quan trọng: Đã bắn thông báo/Email cho ứng viên chưa?
    @Column(name = "is_notified", nullable = false)
    @Builder.Default
    private Boolean isNotified = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}