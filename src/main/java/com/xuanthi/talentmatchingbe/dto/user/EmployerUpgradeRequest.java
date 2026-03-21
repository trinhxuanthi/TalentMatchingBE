package com.xuanthi.talentmatchingbe.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployerUpgradeRequest {
    @NotBlank(message = "Tên công ty không được để trống")
    private String companyName;

    @NotBlank(message = "Mã số thuế không được để trống")
    private String taxCode;

    private String companyAddress;
    private String website;

    @NotBlank(message = "Vui lòng tải lên Giấy phép kinh doanh")
    private String businessLicenseUrl;

    private String position;
}