package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.MatchingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchingSessionRepository extends JpaRepository<MatchingSession, String> {
    // Tạm thời chưa cần custom query nào ở đây, JpaRepository đã lo đủ các lệnh cơ bản
}