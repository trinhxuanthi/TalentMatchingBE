package com.xuanthi.talentmatchingbe.dto.report;

import com.xuanthi.talentmatchingbe.enums.ReportStatus;
import com.xuanthi.talentmatchingbe.enums.ReportType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private Long id;
    private String senderEmail;
    private String title;
    private String content;
    private ReportType type;
    private ReportStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
}