package com.xuanthi.talentmatchingbe.dto.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationRequest {

    @NotNull(message = "ID công việc không được để trống")
    private Long jobId;

    @NotBlank(message = "Vui lòng cung cấp đường dẫn CV")
    private String cvUrl;

    private String coverLetter;
}