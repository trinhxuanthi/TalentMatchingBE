package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Report;
import com.xuanthi.talentmatchingbe.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("SELECT r FROM Report r JOIN FETCH r.sender WHERE (:status IS NULL OR r.status = :status)")
    Page<Report> findAllByStatus(@Param("status") ReportStatus status, Pageable pageable);
}