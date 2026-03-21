package com.xuanthi.talentmatchingbe.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponse {

    // ID của đơn ứng tuyển (để HR bấm vào xem chi tiết)
    private Long applicationId;

    // Thông tin cơ bản để hiển thị trên bảng xếp hạng
    private String candidateName;
    private String cvUrl;

    // Điểm số "ăn tiền" từ AI (Dùng để Sort giảm dần)
    private Double matchScore;

    // Khối phân tích chi tiết (Đã được parse thành Object để Frontend dễ vẽ Tag màu)
    private AiAnalysisDTO aiAnalysis;

    // Thời gian nộp để HR biết ứng viên nộp lâu chưa
    private LocalDateTime appliedAt;
}