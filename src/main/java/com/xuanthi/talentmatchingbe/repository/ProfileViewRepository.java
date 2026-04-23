package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.ProfileView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileViewRepository extends JpaRepository<ProfileView, Long> {
    // Lấy danh sách ai đã xem hồ sơ của mình, sắp xếp mới nhất lên đầu
    Page<ProfileView> findByCandidateIdOrderByViewedAtDesc(Long candidateId, Pageable pageable);
    // Đếm số lượng ứng viên DUY NHẤT mà HR này đã từng xem hồ sơ
    @Query("SELECT COUNT(DISTINCT pv.candidate.id) FROM ProfileView pv WHERE pv.employer.id = :employerId")
    long countUniqueViewsByEmployerId(@Param("employerId") Long employerId);

    // Kiểm tra xem HR này đã từng xem ứng viên này chưa
    boolean existsByEmployerIdAndCandidateId(Long employerId, Long candidateId);
}