package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.JobMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatch, Long> {

    // Tìm xem cặp Ứng viên - Job này đã từng tính điểm chưa (để Update thay vì Insert mới)
    Optional<JobMatch> findByCandidateIdAndJobId(Long candidateId, Long jobId);

    // Dành cho Frontend: Lấy danh sách Job phù hợp của 1 ứng viên, sắp xếp điểm từ cao xuống thấp
    List<JobMatch> findByCandidateIdOrderByMatchScoreDesc(Long candidateId);

}