package com.xuanthi.talentmatchingbe.dto.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JdGenerateReq {

    @JsonProperty("raw_info")
    @NotBlank(message = "Vui lòng nhập yêu cầu tuyển dụng thô.")
    @Size(min = 10, message = "Nhập ít nhất 10 ký tự để AI có đủ thông tin phân tích.")
    private String rawInfo;

}