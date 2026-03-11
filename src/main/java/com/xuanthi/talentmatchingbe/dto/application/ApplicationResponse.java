package com.xuanthi.talentmatchingbe.dto.application;

import com.xuanthi.talentmatchingbe.enums.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateFullName;
    private String candidateEmail;
    private String cvUrl;
    private String coverLetter;
    private ApplicationStatus status;
    private String notes;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}