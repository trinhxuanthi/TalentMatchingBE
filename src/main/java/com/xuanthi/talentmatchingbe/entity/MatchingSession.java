package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "matching_sessions")
@Data
public class MatchingSession {

    @Id
    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "jd_file_name")
    private String jdFileName;

    @Column(name = "jd_text", columnDefinition = "TEXT")
    private String jdText;

    @Column(name = "custom_rules", columnDefinition = "TEXT")
    private String customRules;

    @Column(name = "total_cvs")
    private Integer totalCvs;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}