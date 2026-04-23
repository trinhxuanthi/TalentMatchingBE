package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho kết quả ranking của ứng viên từ AI
 * Lưu trữ điểm số và phân tích chi tiết từ AI matching
 */
@Entity
@Table(name = "candidate_rankings",
       indexes = {
           @Index(name = "idx_candidate_ranking_session", columnList = "session_id"),
           @Index(name = "idx_candidate_ranking_score", columnList = "match_score"),
           @Index(name = "idx_candidate_ranking_created", columnList = "created_at")
       })
@Data
public class CandidateRanking {

    /**
     * ID duy nhất của bản ghi ranking
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID của session AI matching (nhóm nhiều CV trong 1 lần scan)
     */
    @NotBlank(message = "Session ID không được rỗng")
    @Size(max = 100, message = "Session ID không được vượt quá 100 ký tự")
    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    /**
     * Tên file CV của ứng viên
     */
    @NotBlank(message = "Tên file CV không được rỗng")
    @Column(name = "candidate_file", nullable = false)
    private String candidateFile;

    /**
     * Điểm số matching tổng thể (0-100)
     */
    @Column(name = "match_score")
    private Integer matchScore;

    /**
     * Khuyến nghị của AI (ĐỀ XUẤT, XEM XÉT, TỪ CHỐI)
     */
    @Size(max = 50, message = "Recommendation không được vượt quá 50 ký tự")
    @Column(name = "recommendation", length = 50)
    private String recommendation;

    /**
     * Điểm số kinh nghiệm
     */
    @Column(name = "exp_score")
    private Integer expScore;

    /**
     * Điểm số kỹ năng
     */
    @Column(name = "skill_score")
    private Integer skillScore;

    /**
     * Điểm số phù hợp vai trò
     */
    @Column(name = "role_score")
    private Integer roleScore;

    /**
     * JSON string chứa phân tích chi tiết từ AI
     */
    @Column(name = "detailed_analysis_json", columnDefinition = "TEXT")
    private String detailedAnalysisJson;

    /**
     * Thời gian tạo bản ghi
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
