package com.xuanthi.talentmatchingbe.mapper;

import com.xuanthi.talentmatchingbe.dto.company.CompanyDetailResponse;
import com.xuanthi.talentmatchingbe.dto.company.CompanyResponse;
import com.xuanthi.talentmatchingbe.entity.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    /**
     * Map Entity sang DTO thu gọn (Dùng cho List/Page hiển thị ở trang chủ)
     */
    public CompanyResponse toCompanyResponse(Company company) {
        if (company == null) {
            return null;
        }

        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .address(company.getAddress())

                // BỔ SUNG 2 DÒNG NÀY ĐỂ NÓ LẤY DATA TRẢ VỀ JSON:
                .website(company.getWebsite())

                // Lưu ý: Nếu trong Entity sếp đặt tên là 'description'
                // nhưng trong DTO là 'shortDescription' thì map như thế này:
                .shortDescription(company.getDescription())

                .build();
    }

    /**
     * Map Entity sang DTO chi tiết (Dùng cho trang Xem chi tiết công ty)
     */
    public CompanyDetailResponse toCompanyDetailResponse(Company company) {
        if (company == null) {
            return null;
        }

        return CompanyDetailResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .address(company.getAddress())
                .website(company.getWebsite())
                .description(company.getDescription())
                // Nếu sếp muốn lấy email của HR để hiển thị
                .employerEmail(company.getUser() != null ? company.getUser().getEmail() : null)
                .build();
    }
}