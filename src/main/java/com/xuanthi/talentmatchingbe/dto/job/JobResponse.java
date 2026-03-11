package com.xuanthi.talentmatchingbe.dto.job;

import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.enums.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private JobType jobType;
    private String experienceLevel;
    private JobStatus status;
    private LocalDateTime createdAt;

    // TỐI ƯU: Trả về thông tin cơ bản của Employer để Frontend hiển thị luôn
    private Long employerId;
    private String employerName;
    private String employerAvatar;
    private boolean isExpired;
}