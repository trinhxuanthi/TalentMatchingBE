package com.xuanthi.talentmatchingbe.dto.job;

import lombok.Data;

@Data
public class AiWeightDto {
    private Double weightExp = 0.30;
    private Double weightSkills = 0.30;
    private Double weightRole = 0.15;
    private Double weightTools = 0.10;
    private Double weightEdu = 0.10;
    private Double weightSoft = 0.05;
}