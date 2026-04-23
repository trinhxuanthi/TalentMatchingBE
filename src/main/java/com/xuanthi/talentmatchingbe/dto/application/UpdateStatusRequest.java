package com.xuanthi.talentmatchingbe.dto.application;

import com.xuanthi.talentmatchingbe.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private ApplicationStatus status;

    private String notes; // Có thể để trống
}
