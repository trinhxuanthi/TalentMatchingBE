package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_rankings")
@Data
public class CandidateRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId; // Dùng để nhóm 1 đợt HR quét nhiều CV

    @Column(name = "candidate_file", nullable = false)
    private String candidateFile;

    @Column(name = "match_score")
    private Integer matchScore;

    @Column(name = "recommendation", length = 50)
    private String recommendation;

    @Column(name = "exp_score")
    private Integer expScore;

    @Column(name = "skill_score")
    private Integer skillScore;

    @Column(name = "role_score")
    private Integer roleScore;

    // Lưu toàn bộ lời phê của AI thành chuỗi JSON để tối ưu tốc độ Ghi DB
    @Column(name = "detailed_analysis_json", columnDefinition = "TEXT")
    private String detailedAnalysisJson;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
