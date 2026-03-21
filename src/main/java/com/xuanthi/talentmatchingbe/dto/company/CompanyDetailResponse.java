package com.xuanthi.talentmatchingbe.dto.company;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyDetailResponse {
    private Long id;
    private String name;
    private String logoUrl;
    private String address;
    private String website;
    private String description; // Trả về nguyên bài viết dài (có thể chứa HTML)
}