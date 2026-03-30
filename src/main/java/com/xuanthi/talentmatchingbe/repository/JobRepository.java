package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Job;
import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // Lấy bài đăng theo trạng thái (cho ứng viên)
    Page<Job> findAllByStatus(JobStatus status, Pageable pageable);

    // Lấy bài đăng theo chủ sở hữu (cho nhà tuyển dụng)
    Page<Job> findAllByEmployerId(Long employerId, Pageable pageable);

    // Đếm số Job đang mở của Employer
    long countByEmployerIdAndStatus(Long employerId, JobStatus status);

    // Lấy danh sách Job để thống kê chi tiết
    List<Job> findAllByEmployerId(Long employerId);

    // 🔥 Đã tối ưu cách gọi Enum trong JPQL để tránh lỗi Type Mismatch của Hibernate
    @Query("SELECT j FROM Job j WHERE " +
            "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:location IS NULL OR j.location = :location) AND " +
            "(:jobType IS NULL OR j.jobType = :jobType) AND " +
            "(:minSalary IS NULL OR j.salaryMax >= :minSalary) AND " +
            "(j.status = com.xuanthi.talentmatchingbe.enums.JobStatus.OPEN) AND " +
            "(j.deadline IS NULL OR j.deadline > CURRENT_TIMESTAMP)")
    Page<Job> findJobsWithFilters(
            @Param("title") String title,
            @Param("location") String location,
            @Param("jobType") JobType jobType,
            @Param("minSalary") BigDecimal minSalary,
            Pageable pageable);

    // 🔥 ĐỔI TÊN HÀM: Từ findActiveJobs... thành findOpenJobs...
    @Query("SELECT j FROM Job j WHERE j.employer.company.id = :companyId AND j.status = com.xuanthi.talentmatchingbe.enums.JobStatus.OPEN")
    Page<Job> findOpenJobsByCompany(@Param("companyId") Long companyId, Pageable pageable);

    // 🔥 ĐỔI TÊN HÀM: Từ searchActiveJobs... thành searchOpenJobs...
    @Query("SELECT j FROM Job j WHERE j.employer.company.id = :companyId " +
            "AND j.status = com.xuanthi.talentmatchingbe.enums.JobStatus.OPEN AND LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Job> searchOpenJobsByCompany(@Param("companyId") Long companyId, @Param("keyword") String keyword, Pageable pageable);
}