package com.xuanthi.talentmatchingbe.dto.application;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class EmployerDashboardResponse {
    private long totalActiveJobs;      // Tổng số bài đăng đang OPEN
    private long totalApplications;    // Tổng số CV nộp vào tất cả các Job
    private long pendingApplications;  // Số CV mới chưa xử lý (Status = PENDING)
    private long unreadApplications;   // MỚI: Số đơn chưa nhấn vào xem
    private long interviewScheduled;   // Số ứng viên đang chờ phỏng vấn

    // Thống kê theo từng Job: Job Title -> Số lượng CV nộp vào
    private Map<String, Long> applicationsPerJob;
}
