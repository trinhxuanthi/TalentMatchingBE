package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Job;
import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // =========================================================================
    // 1. CÁC HÀM DÀNH CHO HR (GIỮ NGUYÊN - Không cần sort VIP)
    // =========================================================================
    Page<Job> findAllByEmployerId(Long employerId, Pageable pageable);
    long countByEmployerIdAndStatus(Long employerId, JobStatus status);
    List<Job> findAllByEmployerId(Long employerId);
    int countByEmployerId(Long employerId);


    // =========================================================================
    // 2. CÁC HÀM DÀNH CHO ỨNG VIÊN (SỬA LẠI - AUTO ĐẨY VIP LÊN TOP 1)
    // =========================================================================

    // ✅ SỬA 1: Dùng @Query để ép nó sort VIP trước, ngày tạo sau
    @Query("SELECT j FROM Job j WHERE j.status = :status " +
            "ORDER BY j.isPriority DESC, j.createdAt DESC")
    Page<Job> findAllByStatus(@Param("status") JobStatus status, Pageable pageable);

    // ✅ SỬA 2: Thêm ORDER BY vào hàm Filter phức tạp
    @Query("SELECT j FROM Job j WHERE " +
            "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:location IS NULL OR j.location = :location) AND " +
            "(:jobType IS NULL OR j.jobType = :jobType) AND " +
            "(:minSalary IS NULL OR j.salaryMax >= :minSalary) AND " +
            "(j.status = com.xuanthi.talentmatchingbe.enums.JobStatus.OPEN) AND " +
            "(j.deadline IS NULL OR j.deadline > CURRENT_TIMESTAMP) " +
            "ORDER BY j.isPriority DESC, j.createdAt DESC")
    Page<Job> findJobsWithFilters(
            @Param("title") String title,
            @Param("location") String location,
            @Param("jobType") JobType jobType,
            @Param("minSalary") BigDecimal minSalary,
            Pageable pageable);

    // ✅ SỬA 3: Thêm ORDER BY vào tìm Job mở của 1 công ty
    @Query("SELECT j FROM Job j WHERE j.employer.company.id = :companyId AND j.status = com.xuanthi.talentmatchingbe.enums.JobStatus.OPEN " +
            "ORDER BY j.isPriority DESC, j.createdAt DESC")
    Page<Job> findOpenJobsByCompany(@Param("companyId") Long companyId, Pageable pageable);

    // ✅ SỬA 4: Thêm ORDER BY vào tìm kiếm theo Keyword trong công ty
    @Query("SELECT j FROM Job j WHERE j.employer.company.id = :companyId " +
            "AND j.status = com.xuanthi.talentmatchingbe.enums.JobStatus.OPEN AND LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY j.isPriority DESC, j.createdAt DESC")
    Page<Job> searchOpenJobsByCompany(@Param("companyId") Long companyId, @Param("keyword") String keyword, Pageable pageable);

    // Quét và khóa toàn bộ Job có deadline nhỏ hơn ngày hiện tại, và trạng thái chưa bị khóa
    @Modifying
    @Query("UPDATE Job j SET j.status = 'CLOSED' WHERE j.deadline < :today AND j.status != 'CLOSED'")
    int closeExpiredJobs(@Param("today") java.time.LocalDate today);
    @Query(value = "SELECT category, COUNT(*) as count FROM jobs GROUP BY category ORDER BY count DESC LIMIT 5", nativeQuery = true)
    List<Map<String, Object>> getHotJobCategories();

    // Tìm kiếm Job cho Admin: Lọc theo từ khóa (Tiêu đề/Công ty) và Trạng thái
    @Query("SELECT j FROM Job j JOIN FETCH j.employer e " +
            "WHERE (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR j.status = :status)")
    Page<Job> findAllForAdmin(@Param("keyword") String keyword,
                              @Param("status") JobStatus status,
                              Pageable pageable);
}
