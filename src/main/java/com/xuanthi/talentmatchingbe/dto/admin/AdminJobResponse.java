package com.xuanthi.talentmatchingbe.dto.admin;

import com.xuanthi.talentmatchingbe.enums.JobStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminJobResponse {
    private Long id;
    private String title;          // Tiêu đề công việc
    private String companyName;    // Tên công ty đăng tuyển
    private String employerEmail;  // Email của HR đăng bài
    private JobStatus status;      // Trạng thái (OPEN, CLOSED, BANNED...)
    private LocalDateTime createdAt;
    private long totalApplications; // Tổng số ứng viên đã nộp vào bài này
}