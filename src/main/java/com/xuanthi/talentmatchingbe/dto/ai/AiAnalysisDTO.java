package com.xuanthi.talentmatchingbe.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisDTO {

    // Điểm tổng quát (Để dự phòng trường hợp cần parse thẳng từ Python)
    @JsonProperty("match_score")
    private Double matchScore;

    // Điểm thành phần (VD: {"skills": "40/50", "experience": "45/50"})
    @JsonProperty("score_breakdown")
    private Map<String, String> scoreBreakdown;

    // Phân tích kỹ năng cực kỳ chi tiết cho UI
    @JsonProperty("skills_analysis")
    private SkillsAnalysis skillsAnalysis;

    // Phân tích ngữ nghĩa từ SBERT (VD: "Ứng viên có kinh nghiệm Microservices rất tốt...")
    @JsonProperty("experience_analysis")
    private String experienceAnalysis;

    // Nhận xét tổng quan (VD: "Rất phù hợp để phỏng vấn vòng 1")
    private String conclusion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillsAnalysis {

        // --- Nhóm kỹ năng BẮT BUỘC (Trọng số cao - Vẽ Tag Đỏ) ---
        @JsonProperty("matched_must_have")
        private List<String> matchedMustHave;

        @JsonProperty("missing_must_have")
        private List<String> missingMustHave;

        // --- Nhóm kỹ năng ƯU TIÊN (Trọng số thấp - Vẽ Tag Xanh) ---
        @JsonProperty("matched_nice_to_have")
        private List<String> matchedNiceToHave;

        @JsonProperty("missing_nice_to_have")
        private List<String> missingNiceToHave;
    }
}