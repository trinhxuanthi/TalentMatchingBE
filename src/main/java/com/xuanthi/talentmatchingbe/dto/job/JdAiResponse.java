package com.xuanthi.talentmatchingbe.dto.job;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
// 🚨 VŨ KHÍ TỐI THƯỢNG: Nếu AI vui tính đẻ thêm một trường lạ (VD: "note": "..."),
// Java sẽ tự động bơ đi mà không bị báo lỗi sập server (500 Internal Server Error).
@JsonIgnoreProperties(ignoreUnknown = true)
public class JdAiResponse {
    private String title;
    private String location;
    private Long salaryMin;
    private Long salaryMax;
    private OffsetDateTime deadline;
    private String jobType;
    private String experienceLevel;
    private Integer minExpYears;
    private String jobLevel;
    private String educationLevel;
    private Integer quantity;
    private String description;
    private String benefits;
    private String requirements;
    private List<String> requiredSkills;
    private List<String> categories;
    private Boolean salaryNegotiable;
}