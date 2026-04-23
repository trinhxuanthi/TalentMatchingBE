package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. NGƯỜI THANH TOÁN
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 2. GÓI CƯỚC ĐÃ MUA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private PricingPlan plan;

    // 3. THÔNG TIN GIAO DỊCH
    @Column(name = "amount", nullable = false)
    private Double amount; // Số tiền thực tế đã thanh toán

    @Column(name = "txn_ref", unique = true, nullable = false)
    private String txnRef; // Mã giao dịch của VNPay (Ví dụ: 1_2_170000000)

    @Column(name = "status", length = 20, nullable = false)
    private String status; // Trạng thái: "SUCCESS", "FAILED", "PENDING"

    @Column(name = "bank_code", length = 20)
    private String bankCode; // Mã ngân hàng (NCB, VCB...) lấy từ VNPay

    // 4. THỜI GIAN
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}