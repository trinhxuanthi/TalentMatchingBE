package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.admin.DashboardStatsResponse;
import com.xuanthi.talentmatchingbe.repository.ApplicationRepository;
import com.xuanthi.talentmatchingbe.repository.JobRepository;
import com.xuanthi.talentmatchingbe.repository.PaymentRepository;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    // Lưu ý: Nếu sếp đặt tên Repo thanh toán khác (ví dụ TransactionRepository) thì sửa ở đây nhé
    private final PaymentRepository paymentRepository;


    @Transactional(readOnly = true)
    public DashboardStatsResponse getFullAnalytics() {
        log.info("📊 Đang tổng hợp dữ liệu cho Admin Dashboard...");

        // 1. CHỐNG LỖI NULL (Rất quan trọng khi DB mới tinh, chưa có dữ liệu)
        Double rawRevenue = paymentRepository.getTotalSuccessAmount();
        double totalRevenue = (rawRevenue != null) ? rawRevenue : 0.0;

        Double rawAvgScore = applicationRepository.getAverageSuccessfulMatchingScore();
        double matchingRate = (rawAvgScore != null) ? rawAvgScore : 0.0;

        // 2. GOM DỮ LIỆU THỐNG KÊ BIỂU ĐỒ
        List<Map<String, Object>> statusStats = formatStats(applicationRepository.getStatusDistribution());

        // Nếu các hàm getHotJobCategories và getMonthlyRevenue chưa viết, sếp có thể tạm comment 2 dòng dưới lại
        List<Map<String, Object>> categoryStats = jobRepository.getHotJobCategories();
        List<Map<String, Object>> revenueGrowth = paymentRepository.getMonthlyRevenue();

        // 3. ĐÓNG GÓI VÀ TRẢ VỀ FRONTEND
        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                // Tạm đếm tổng số Job. Nếu sếp có trạng thái (Ví dụ JobStatus.OPEN) thì dùng: jobRepository.countByStatus(JobStatus.OPEN)
                .activeJobs(jobRepository.count())
                .totalApplications(applicationRepository.count())
                .totalRevenue(totalRevenue)

                // Làm tròn điểm AI (Ví dụ 85.6789 -> 85.68)
                .matchingSuccessRate(Math.round(matchingRate * 100.0) / 100.0)

                .applicationStatusStats(statusStats)
                .jobCategoryStats(categoryStats)
                .revenueGrowth(revenueGrowth)
                .build();
    }

    /**
     * Hàm phụ trợ (Mapper): Biến đổi mảng Object[] thô từ SQL thành Map chuẩn JSON cho Chart.js / Recharts
     * @param rawData Dữ liệu từ Repository trả về dưới dạng List<Object[]>
     * @return List<Map> chuẩn cấu trúc [{label: "ACCEPTED", value: 10}]
     */
    private List<Map<String, Object>> formatStats(List<Object[]> rawData) {
        if (rawData == null || rawData.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (Object[] row : rawData) {
            Map<String, Object> map = new HashMap<>();
            // Cột 0: Tên (Label)
            map.put("label", row[0] != null ? row[0].toString() : "UNKNOWN");
            // Cột 1: Số lượng (Value)
            map.put("value", row[1] != null ? ((Number) row[1]).longValue() : 0L);

            formattedList.add(map);
        }
        return formattedList;
    }
}