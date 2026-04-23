package com.xuanthi.talentmatchingbe.dto.payment;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PricingPlanResponse {
    private Long id;
    private String name;
    private String description;
    private Integer durationDays;
    private BigDecimal basePrice;
    private Integer discountPercent;
    private BigDecimal finalPrice; // Trả thẳng giá cuối cho Frontend đỡ phải tính
}