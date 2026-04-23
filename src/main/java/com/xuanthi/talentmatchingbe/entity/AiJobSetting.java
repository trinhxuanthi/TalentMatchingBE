package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_job_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiJobSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Gán luôn giá trị mặc định, phòng trường hợp HR lười không nhập
    @Builder.Default private Double weightExp = 0.30;
    @Builder.Default private Double weightSkills = 0.30;
    @Builder.Default private Double weightRole = 0.15;
    @Builder.Default private Double weightTools = 0.10;
    @Builder.Default private Double weightEdu = 0.10;
    @Builder.Default private Double weightSoft = 0.05;
}