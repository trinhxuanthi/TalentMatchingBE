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

    // Kiểm tra xem ứng viên đã nộp đơn vào Job này chưa (Chống Spam)
    // Thêm check isActive để nếu họ đã rút đơn cũ thì mới được nộp đơn mới
    boolean existsByJobIdAndCandidateIdAndIsActiveTrue(Long jobId, Long candidateId);

    // Cho Employer: Xem danh sách ứng viên nộp vào 1 Job cụ thể
    Page<Application> findByJobIdOrderByAppliedAtDesc(Long jobId, Pageable pageable);

    // Cho Candidate: Xem lại lịch sử các Job mình đã nộp
    Page<Application> findByCandidateIdOrderByAppliedAtDesc(Long candidateId, Pageable pageable);

    // Tìm đơn ứng tuyển kèm theo check quyền sở hữu (Để bảo mật khi update status)
    Optional<Application> findByIdAndJobEmployerId(Long appId, Long employerId);

    // Đếm tổng số đơn của 1 ứng viên
    long countByCandidateId(Long candidateId);

    // Đếm theo trạng thái cụ thể của ứng viên đó
    long countByCandidateIdAndStatus(Long candidateId, ApplicationStatus status);

    // Đếm tổng số đơn nộp vào các Job của 1 Employer cụ thể
    long countByJobEmployerId(Long employerId);

    // Đếm số đơn theo trạng thái cụ thể của 1 Employer
    long countByJobEmployerIdAndStatus(Long employerId, ApplicationStatus status);

    // Đếm số đơn nộp vào 1 Job cụ thể
    long countByJobId(Long jobId);

    @Query(value = "SELECT DATE_FORMAT(applied_at, '%Y-%m') as month, COUNT(*) as count " +
            "FROM applications a " +
            "JOIN jobs j ON a.job_id = j.id " +
            "WHERE j.employer_id = :employerId " +
            "AND a.applied_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH) " +
            "GROUP BY month " +
            "ORDER BY month ASC", nativeQuery = true)
    List<Object[]> getMonthlyStatsNative(Long employerId);

    // Đếm số lượng đơn chưa xem của một Nhà tuyển dụng cụ thể
    long countByJobEmployerIdAndIsViewedFalse(Long employerId);

    // Chỉ lấy những đơn thuộc Job này và đã được chấm điểm, sắp xếp giảm dần
    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId " +
            "ORDER BY a.matchScore DESC, a.appliedAt ASC")
    List<Application> findRankingByJobId(@Param("jobId") Long jobId);
}