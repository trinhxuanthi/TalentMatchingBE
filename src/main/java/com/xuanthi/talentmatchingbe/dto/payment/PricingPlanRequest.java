package com.xuanthi.talentmatchingbe.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PricingPlanRequest {

    @NotBlank(message = "Tên gói không được để trống")
    private String name;

    private String description;

    @NotNull
    @Min(value = 1, message = "Số ngày phải lớn hơn 0")
    private Integer durationDays;

    @NotNull
    @Min(value = 0, message = "Giá gốc không được âm")
    private BigDecimal basePrice;

    @Min(value = 0, message = "Giảm giá nhỏ nhất là 0%")
    private Integer discountPercent;

    @NotBlank(message = "Phân loại đối tượng không được để trống (EMPLOYER/CANDIDATE)")
    private String targetRole;
}