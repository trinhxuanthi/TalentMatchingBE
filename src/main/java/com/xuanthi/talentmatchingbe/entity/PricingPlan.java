package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PricingPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Integer durationDays;
    private BigDecimal basePrice;
    private Integer discountPercent;
    private String targetRole;
    private boolean isActive;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ✅ HÀM TÍNH GIÁ THỰC TẾ (Dùng để hiển thị và gửi cho VNPay)
    public BigDecimal getFinalPrice() {
        if (discountPercent == null || discountPercent <= 0) return basePrice;
        BigDecimal discount = basePrice.multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100));
        return basePrice.subtract(discount);
    }
}