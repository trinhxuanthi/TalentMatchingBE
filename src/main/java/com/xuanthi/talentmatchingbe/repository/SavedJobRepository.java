package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Job;
import com.xuanthi.talentmatchingbe.entity.SavedJob;
import com.xuanthi.talentmatchingbe.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho SavedJob entity
 * Quản lý các thao tác database liên quan đến việc lưu công việc
 */
@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    /**
     * Tìm xem ứng viên đã lưu job này chưa (Dùng để bật/tắt nút Heart)
     * @param candidate ứng viên cần kiểm tra
     * @param job công việc cần kiểm tra
     * @return Optional chứa SavedJob nếu đã lưu, empty nếu chưa lưu
     */
    Optional<SavedJob> findByCandidateAndJob(User candidate, Job job);

    /**
     * Kiểm tra nhanh xem đã lưu chưa (trả về true/false)
     * @param candidate ứng viên cần kiểm tra
     * @param job công việc cần kiểm tra
     * @return true nếu đã lưu, false nếu chưa lưu
     */
    boolean existsByCandidateAndJob(User candidate, Job job);

    /**
     * Lấy danh sách việc làm đã lưu của 1 ứng viên
     * @param candidate ứng viên cần lấy danh sách
     * @param pageable thông tin phân trang và sắp xếp
     * @return Page chứa danh sách SavedJob
     */
    Page<SavedJob> findByCandidate(User candidate, Pageable pageable);

    /**
     * Lấy danh sách job IDs đã lưu của một ứng viên
     * Tối ưu cho việc kiểm tra batch
     * @param candidate ứng viên cần lấy danh sách
     * @return List các job IDs đã lưu
     */
    @Query("SELECT sj.job.id FROM SavedJob sj WHERE sj.candidate = :candidate")
    List<Long> findSavedJobIdsByCandidate(@Param("candidate") User candidate);

    /**
     * Đếm số lượng công việc đã lưu của một ứng viên
     * @param candidate ứng viên cần đếm
     * @return số lượng công việc đã lưu
     */
    long countByCandidate(User candidate);
}