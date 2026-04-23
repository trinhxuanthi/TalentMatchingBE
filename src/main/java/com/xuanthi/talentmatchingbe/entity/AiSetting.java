package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "ai_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSetting {

    @Id
    private Long id; // Luôn là 1L

    // === QUYỀN ADMIN 1: LỌC RÁC ĐẦU VÀO ===
    private Double filterTitleScore; // VD: 65.0
    private Double filterTotalScore; // VD: 50.0
    private Double sbertRejectionThreshold; // Ngưỡng AI Lọc Nhanh (VD: 0.4)

    // === QUYỀN ADMIN 2: GỢI Ý JOB CHO ỨNG VIÊN (Luồng chạy ngầm) ===
    private Double sbertWeightTitle;  // VD: 0.45
    private Double sbertWeightSkills; // VD: 0.40
    private Double sbertWeightExp;    // VD: 0.15
}