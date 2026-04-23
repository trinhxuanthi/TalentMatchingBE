package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {
    // Tự định nghĩa hàm tìm Profile theo ID của User
    Optional<CandidateProfile> findByUserId(Long userId);
}