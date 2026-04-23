package com.xuanthi.talentmatchingbe.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileViewResponse {
    // Thông tin người xem (HR)
    private Long employerId;
    private String employerName;

    // ✅ THÊM THÔNG TIN CÔNG TY (Cái này ăn tiền nhất)
    private Long companyId;
    private String companyName;
    private String companyLogo; // Avatar của công ty

    private LocalDateTime viewedAt;
}