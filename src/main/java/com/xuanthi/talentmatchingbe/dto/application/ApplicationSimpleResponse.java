package com.xuanthi.talentmatchingbe.dto.application;

import com.xuanthi.talentmatchingbe.enums.ApplicationStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationSimpleResponse {
    private Long id;
    private Long candidateId;
    private String candidateFullName;
    private String candidateEmail;
    private String cvUrl;
    private Integer matchScore;
    private String aiRecommendation; // Giữ lại cái này để hiện nhãn Đề xuất/Từ chối
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
}