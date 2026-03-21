package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Job;
import com.xuanthi.talentmatchingbe.entity.SavedJob;
import com.xuanthi.talentmatchingbe.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    // Tìm xem ứng viên đã lưu job này chưa (Dùng để bật/tắt nút Heart)
    Optional<SavedJob> findByCandidateAndJob(User candidate, Job job);

    // Kiểm tra nhanh xem đã lưu chưa (trả về true/false)
    boolean existsByCandidateAndJob(User candidate, Job job);

    // Lấy danh sách việc làm đã lưu của 1 ứng viên
    Page<SavedJob> findByCandidate(User candidate, Pageable pageable);
}