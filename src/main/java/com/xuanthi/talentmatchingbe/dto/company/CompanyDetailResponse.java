package com.xuanthi.talentmatchingbe.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyDetailResponse {
    @Positive(message = "ID phải lớn hơn 0")
    private Long id;

    @NotBlank(message = "Tên công ty không được để trống")
    @Size(max = 200, message = "Tên không được quá 200 ký tự")
    private String name;

    @Size(max = 500, message = "Logo URL không được quá 500 ký tự")
    private String logoUrl;

    @Size(max = 255, message = "Địa chỉ không được quá 255 ký tự")
    private String address;

    @Size(max = 255, message = "Website không được quá 255 ký tự")
    private String website;

    @Size(max = 5000, message = "Mô tả không được quá 5000 ký tự")
    private String description; // Trả về nguyên bài viết dài (có thể chứa HTML)

    private String employerEmail;
}