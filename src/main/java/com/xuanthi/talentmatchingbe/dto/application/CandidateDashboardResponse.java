package com.xuanthi.talentmatchingbe.dto.application;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CandidateDashboardResponse {
    private long totalApplied;    // Tổng số Job đã nộp
    private long pendingCount;    // Đang chờ phản hồi
    private long interviewCount;  // Được mời phỏng vấn
    private long acceptedCount;   // Đã trúng tuyển
    private long rejectedCount;   // Bị từ chối
}