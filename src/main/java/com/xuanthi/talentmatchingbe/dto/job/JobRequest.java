package com.xuanthi.talentmatchingbe.dto.job;

import com.xuanthi.talentmatchingbe.enums.JobType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JobRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    private String description;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private JobType jobType;
    private String experienceLevel;
    private LocalDateTime deadline;
}
