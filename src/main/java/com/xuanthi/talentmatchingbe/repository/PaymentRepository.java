package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // =====================================================================
    // 1. TÍNH TỔNG DOANH THU TỪ TRƯỚC ĐẾN NAY
    // Dùng JPQL tương tác trực tiếp với Entity Payment
    // =====================================================================
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS'")
    Double getTotalSuccessAmount();


    // =====================================================================
    // 2. THỐNG KÊ DOANH THU 6 THÁNG GẦN NHẤT (Vẽ biểu đồ Line/Bar Chart)
    // Phải dùng Native Query vì các hàm xử lý ngày tháng (DATE_FORMAT) của MySQL
    // hoạt động tốt nhất khi viết SQL thuần.
    // =====================================================================
    @Query(value = "SELECT " +
            "   DATE_FORMAT(created_at, '%m/%Y') AS label, " +
            "   SUM(amount) AS value " +
            "FROM payments " +
            "WHERE status = 'SUCCESS' " +
            "GROUP BY label, DATE_FORMAT(created_at, '%Y%m') " +
            "ORDER BY DATE_FORMAT(created_at, '%Y%m') DESC " +
            "LIMIT 6",
            nativeQuery = true)
    List<Map<String, Object>> getMonthlyRevenue();

}