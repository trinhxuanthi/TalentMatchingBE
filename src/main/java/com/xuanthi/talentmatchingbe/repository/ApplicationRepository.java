package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Application;
import com.xuanthi.talentmatchingbe.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // ==========================================
    // CÁC HÀM XÁC THỰC & BẢO MẬT
    // ==========================================
    // Kiểm tra xem ứng viên đã nộp đơn vào Job này chưa (Chống Spam)
    boolean existsByJobIdAndCandidateIdAndIsActiveTrue(Long jobId, Long candidateId);

    // Kiểm tra xem Email này đã nộp cho Job này bao giờ chưa?
    boolean existsByJobIdAndEmail(Long jobId, String email);

    // Tìm đơn ứng tuyển kèm theo check quyền sở hữu (Để bảo mật khi update status)
    Optional<Application> findByIdAndJobEmployerId(Long appId, Long employerId);


    // ==========================================
    // CÁC HÀM THỐNG KÊ (ĐẾM SỐ LƯỢNG)
    // ==========================================
    long countByCandidateId(Long candidateId);

    long countByCandidateIdAndStatus(Long candidateId, ApplicationStatus status);

    long countByJobEmployerId(Long employerId);

    long countByJobEmployerIdAndStatus(Long employerId, ApplicationStatus status);

    long countByJobId(Long jobId);

    // Đếm số đơn ứng tuyển theo trạng thái (Ví dụ: PENDING là chưa xem)

    @Query(value = "SELECT DATE_FORMAT(applied_at, '%Y-%m') as month, COUNT(*) as count " +
            "FROM applications a " +
            "JOIN jobs j ON a.job_id = j.id " +
            "WHERE j.employer_id = :employerId " +
            "AND a.applied_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH) " +
            "GROUP BY month " +
            "ORDER BY month ASC", nativeQuery = true)
    List<Object[]> getMonthlyStatsNative(Long employerId);


    // ==========================================
    // CÁC HÀM LẤY DANH SÁCH (CHO ỨNG VIÊN)
    // ==========================================
    // Cho Candidate: Xem lại lịch sử các Job mình đã nộp
    Page<Application> findByCandidateIdOrderByAppliedAtDesc(Long candidateId, Pageable pageable);


    // ==========================================
    // 🔥 CÁC HÀM LẤY DANH SÁCH (CHO HR) - AUTO VIP LÊN TOP
    // ==========================================

    // 1. DÀNH CHO HR: Xem danh sách ứng viên (Ưu tiên PRO lên đầu, sau đó mới tính ngày nộp)
    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId " +
            "ORDER BY CASE WHEN a.candidate.accountType = 'PRO' THEN 1 ELSE 2 END ASC, " +
            "a.appliedAt DESC")
    Page<Application> findByJobIdPriority(@Param("jobId") Long jobId, Pageable pageable);

    // 2. DÀNH CHO HR: Xem bảng xếp hạng AI (Ưu tiên PRO lên đầu, sau đó tính điểm AI, cuối cùng là ngày nộp)
    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId " +
            "ORDER BY CASE WHEN a.candidate.accountType = 'PRO' THEN 1 ELSE 2 END ASC, " +
            "a.matchScore DESC, " +
            "a.appliedAt ASC")
    List<Application> findRankingByJobIdPriority(@Param("jobId") Long jobId);


    // Tính tỉ lệ Matching thành công (Tính trung bình điểm AI của những người ĐÃ TRÚNG TUYỂN)
    @Query("SELECT AVG(a.matchScore) FROM Application a WHERE a.status = 'ACCEPTED'")
    Double getAverageSuccessfulMatchingScore();

    // Đếm số lượng đơn theo từng trạng thái (ACCEPTED, REJECTED, PENDING...)
    @Query("SELECT a.status, COUNT(a) FROM Application a GROUP BY a.status")
    List<Object[]> getStatusDistribution();
}