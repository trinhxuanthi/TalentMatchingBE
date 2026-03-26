package com.xuanthi.talentmatchingbe.dto.company;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminCompanyResponse {
    private Long id;
    private String name;
    private String taxCode;
    private String address;
    private String website;
    private String businessLicenseUrl;
    private String hrPosition;
    private String approvalStatus;

    // Lấy thêm email của người nộp để Admin dễ liên lạc
    private String applicantEmail;
    private String applicantName;
}