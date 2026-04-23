package com.xuanthi.talentmatchingbe.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyUpdateRequest {

    @NotBlank(message = "Tên công ty không được để trống")
    @Size(max = 255, message = "Tên công ty không được vượt quá 255 ký tự")
    private String name;

    @NotBlank(message = "Địa chỉ công ty không được để trống")
    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;

    // Website có thể null hoặc rỗng, nhưng nếu có thì sếp có thể gắn thêm @URL để validate
    @Size(max = 255, message = "Website không được vượt quá 255 ký tự")
    private String website;

    @NotBlank(message = "Mô tả công ty không được để trống")
    // Mô tả thường dài, cho phép tối đa 3000 hoặc 5000 ký tự tùy sếp
    @Size(max = 5000, message = "Mô tả công ty quá dài, tối đa 5000 ký tự")
    private String description;

    @NotBlank(message = "Mã số thuế không được để trống!")
    @Size(min = 10, max = 20, message = "Mã số thuế phải từ 10-20 ký tự")
    private String taxCode;
}