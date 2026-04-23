package com.xuanthi.talentmatchingbe.dto.application;

import lombok.Builder;
import lombok.Data;

/**
 * DTO cho dashboard của candidate
 * Hiển thị thống kê đơn ứng tuyển
 */
@Data
@Builder
public class CandidateDashboardResponse {

    /**
     * Tổng số job đã nộp đơn
     */
    private long totalApplied;

    /**
     * Số đơn đang chờ phản hồi
     */
    private long pendingCount;

    /**
     * Số đơn được mời phỏng vấn
     */
    private long interviewCount;

    /**
     * Số đơn đã trúng tuyển
     */
    private long acceptedCount;

    /**
     * Số đơn bị từ chối
     */
    private long rejectedCount;
}