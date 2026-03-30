package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.CandidateRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRankingRepository extends JpaRepository<CandidateRanking, Long> {
    // Truy xuất bảng xếp hạng của 1 đợt quét, điểm từ cao xuống thấp
    List<CandidateRanking> findAllBySessionIdOrderByMatchScoreDesc(String sessionId);
}
