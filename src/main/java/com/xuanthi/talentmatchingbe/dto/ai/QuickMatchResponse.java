package com.xuanthi.talentmatchingbe.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickMatchResponse {
    private Double matchScore;
    private AiAnalysisDTO aiAnalysis;
}