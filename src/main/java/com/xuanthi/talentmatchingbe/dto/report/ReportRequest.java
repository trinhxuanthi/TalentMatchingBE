package com.xuanthi.talentmatchingbe.dto.report;

import com.xuanthi.talentmatchingbe.enums.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    @NotNull(message = "Vui lòng chọn loại báo cáo")
    private ReportType type;
}