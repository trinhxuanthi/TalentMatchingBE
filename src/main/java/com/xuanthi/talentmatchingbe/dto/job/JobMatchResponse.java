package com.xuanthi.talentmatchingbe.dto.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class JobMatchResponse {

    // 1. Dành cho Ứng viên xem danh sách Job (Chỉ hiện thông tin Job)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForCandidate {
        private Long matchId;
        private Long jobId;
        private String jobTitle;
        private Double matchScore;
        private Double skillScore;
        private Double titleScore;
        private Double expScore;
        private String matchedAt; // Đổi ngày giờ sang chuỗi (String) cho Frontend dễ hiển thị
    }

}