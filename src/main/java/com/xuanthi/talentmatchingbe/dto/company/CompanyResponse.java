package com.xuanthi.talentmatchingbe.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyResponse {
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

    // Tùy chọn: Có thể trả về 1 đoạn ngắn gọn của phần mô tả để làm preview
    @Size(max = 150, message = "Mô tả ngắn không được quá 150 ký tự")
    private String shortDescription;
}