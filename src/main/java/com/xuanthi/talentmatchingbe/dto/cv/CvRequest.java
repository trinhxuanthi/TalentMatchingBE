package com.xuanthi.talentmatchingbe.dto.cv;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CvRequest {
    @NotBlank(message = "CV URL không được để trống")
    @Size(max = 500, message = "CV URL không được quá 500 ký tự")
    private String cvUrl;
}