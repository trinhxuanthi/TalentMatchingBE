package com.xuanthi.talentmatchingbe.dto.application;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

/**
 * DTO cho dashboard của employer
 * Hiển thị thống kê công việc và ứng tuyển
 */
@Data
@Builder
public class EmployerDashboardResponse {

    /**
     * Tổng số bài đăng đang OPEN
     */
    private long totalActiveJobs;

    /**
     * Tổng số CV nộp vào tất cả các Job
     */
    private long totalApplications;

    /**
     * Số CV mới chưa xử lý (Status = PENDING)
     */
    private long pendingApplications;

    /**
     * Số đơn chưa nhấn vào xem
     */
    private long unreadApplications;

    /**
     * Số ứng viên đang chờ phỏng vấn
     */
    private long interviewScheduled;

    /**
     * Thống kê theo từng Job: Job Title -> Số lượng CV nộp vào
     */
    private Map<String, Long> applicationsPerJob;
}
