package com.xuanthi.talentmatchingbe.dto.company;

import lombok.Builder;
import lombok.Data;

/**
 * DTO cho admin xem danh sách company pending approval
 * Chứa thông tin công ty và người nộp đơn
 */
@Data
@Builder
public class AdminCompanyResponse {

    /**
     * ID của company
     */
    private Long id;

    /**
     * Tên công ty
     */
    private String name;

    /**
     * Mã số thuế
     */
    private String taxCode;

    /**
     * Địa chỉ công ty
     */
    private String address;

    /**
     * Website công ty
     */
    private String website;

    /**
     * URL giấy phép kinh doanh
     */
    private String businessLicenseUrl;

    /**
     * Chức vụ của người đại diện
     */
    private String hrPosition;

    /**
     * Trạng thái phê duyệt
     */
    private String approvalStatus;

    /**
     * Email của người nộp đơn (để admin liên lạc)
     */
    private String applicantEmail;

    /**
     * Tên của người nộp đơn
     */
    private String applicantName;
}