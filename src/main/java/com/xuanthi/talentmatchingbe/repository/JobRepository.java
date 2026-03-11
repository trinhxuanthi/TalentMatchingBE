package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Job;
import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query("SELECT j FROM Job j WHERE " +
            "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:location IS NULL OR j.location = :location) AND " +
            "(:jobType IS NULL OR j.jobType = :jobType) AND " +
            "(:minSalary IS NULL OR j.salaryMax >= :minSalary) AND " +
            "(j.status = 'OPEN') AND " +
            "(j.deadline IS NULL OR j.deadline > CURRENT_TIMESTAMP)")
    Page<Job> findJobsWithFilters(
            String title,
            String location,
            JobType jobType,
            BigDecimal minSalary,
            Pageable pageable);
}
