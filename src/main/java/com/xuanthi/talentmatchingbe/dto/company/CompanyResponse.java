package com.xuanthi.talentmatchingbe.dto.company;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyResponse {
    private Long id;
    private String name;
    private String logoUrl;
    private String address;
    private String website;

    // Tùy chọn: Có thể trả về 1 đoạn ngắn gọn của phần mô tả để làm preview
    private String shortDescription;
}