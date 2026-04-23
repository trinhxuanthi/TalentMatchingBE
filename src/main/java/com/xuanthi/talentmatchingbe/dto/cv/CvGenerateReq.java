package com.xuanthi.talentmatchingbe.dto.cv;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CvGenerateReq {

    @JsonProperty("raw_info")
    @NotBlank(message = "Thông tin kinh nghiệm không được để trống.")
    @Size(min = 10, message = "Vui lòng nhập ít nhất 10 ký tự để AI có đủ dữ liệu phân tích.")
    private String rawInfo;

}
