package com.xuanthi.talentmatchingbe.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho yêu cầu nâng cấp tài khoản thành Employer
 * Chứa thông tin công ty và giấy tờ cần thiết để xác minh
 */
@Data
public class EmployerUpgradeRequest {

    /**
     * Tên công ty (bắt buộc, 2-200 ký tự)
     */
    @NotBlank(message = "Tên công ty không được để trống")
    @Size(min = 2, max = 200, message = "Tên công ty phải từ 2-200 ký tự")
    private String companyName;

    /**
     * Mã số thuế công ty (bắt buộc, 10-20 ký tự, chỉ số)
     */
    @NotBlank(message = "Mã số thuế không được để trống")
    @Size(min = 10, max = 20, message = "Mã số thuế phải từ 10-20 ký tự")
    @Pattern(regexp = "^[0-9]+$", message = "Mã số thuế chỉ được chứa số")
    private String taxCode;

    /**
     * Địa chỉ công ty (tùy chọn, tối đa 500 ký tự)
     */
    @Size(max = 500, message = "Địa chỉ công ty không được vượt quá 500 ký tự")
    private String companyAddress;

    /**
     * Website công ty (tùy chọn, phải là URL hợp lệ nếu có)
     */
    @Pattern(regexp = "^$|^https?://.*", message = "Website phải là URL hợp lệ (http:// hoặc https://)")
    @Size(max = 200, message = "Website không được vượt quá 200 ký tự")
    private String website;

    /**
     * URL của giấy phép kinh doanh đã upload (bắt buộc)
     */
    @NotBlank(message = "Vui lòng tải lên Giấy phép kinh doanh")
    @Pattern(regexp = "^https?://.*", message = "URL giấy phép kinh doanh không hợp lệ")
    @Size(max = 500, message = "URL giấy phép kinh doanh không được vượt quá 500 ký tự")
    private String businessLicenseUrl;

    /**
     * Chức vụ của người đại diện (tùy chọn, 2-100 ký tự)
     */
    @Size(min = 2, max = 100, message = "Chức vụ phải từ 2-100 ký tự")
    private String position;
}