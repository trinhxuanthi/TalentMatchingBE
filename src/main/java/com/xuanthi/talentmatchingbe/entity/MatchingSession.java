package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho session AI matching
 * Lưu trữ thông tin về một lần scan CV của HR
 */
@Entity
@Table(name = "matching_sessions",
       indexes = {
           @Index(name = "idx_matching_session_created", columnList = "created_at")
       })
@Data
public class MatchingSession {

    /**
     * ID duy nhất của session (UUID)
     */
    @Id
    @NotBlank(message = "Session ID không được rỗng")
    @Size(max = 100, message = "Session ID không được vượt quá 100 ký tự")
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * Tên file JD hoặc tiêu đề job
     */
    @Size(max = 500, message = "JD file name không được vượt quá 500 ký tự")
    @Column(name = "jd_file_name")
    private String jdFileName;

    /**
     * Nội dung JD (TEXT để lưu nhiều nội dung)
     */
    @Column(name = "jd_text", columnDefinition = "TEXT")
    private String jdText;

    /**
     * Quy tắc tùy chỉnh cho AI
     */
    @Column(name = "custom_rules", columnDefinition = "TEXT")
    private String customRules;

    /**
     * Tổng số CV được xử lý trong session này
     */
    @Column(name = "total_cvs")
    private Integer totalCvs;

    /**
     * Thời gian tạo session
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}