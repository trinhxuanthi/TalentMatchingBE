package com.xuanthi.talentmatchingbe.dto.admin;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStatsResponse {
    // --- 1. Nhóm chỉ số "Nóng" (Summary Cards) ---
    private long totalUsers;
    private long totalEmployers;
    private long totalCandidates;
    private long activeJobs;
    private long totalApplications;
    private double totalRevenue;
    private double matchingSuccessRate; // Tỉ lệ khớp lệnh thành công (AI score cao + Accepted)

    // --- 2. Nhóm tăng trưởng (Biểu đồ đường/cột) ---
    private List<Map<String, Object>> revenueGrowth; // Doanh thu theo thời gian
    private List<Map<String, Object>> userGrowth;    // Người dùng mới theo ngày/tháng

    // --- 3. Nhóm cơ cấu (Biểu đồ tròn) ---
    private List<Map<String, Object>> applicationStatusStats; // Phân bổ trạng thái đơn (Accepted, Rejected...)
    private List<Map<String, Object>> jobCategoryStats;      // Nhóm ngành nghề nào đang hot nhất

    // --- 4. Nhóm hiệu năng (Top List) ---
    private List<Map<String, Object>> topCompanies;  // Công ty tích cực nhất
    private List<Map<String, Object>> topSkills;     // Kỹ năng ứng viên tìm kiếm nhiều nhất
}