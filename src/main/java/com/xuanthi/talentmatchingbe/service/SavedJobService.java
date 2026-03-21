package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.job.JobResponse;
import com.xuanthi.talentmatchingbe.entity.Job;
import com.xuanthi.talentmatchingbe.entity.SavedJob;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.mapper.JobMapper; // Giả sử bạn có class Mapper này
import com.xuanthi.talentmatchingbe.repository.JobRepository;
import com.xuanthi.talentmatchingbe.repository.SavedJobRepository;
import com.xuanthi.talentmatchingbe.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper; // Hoặc bạn tự build DTO như các bài trước

    // 1. Logic Bật/Tắt Lưu công việc
    @Transactional
    public String toggleSaveJob(Long jobId) {
        User candidate = SecurityUtils.getCurrentUser();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Công việc không tồn tại!"));

        Optional<SavedJob> existingSavedJob = savedJobRepository.findByCandidateAndJob(candidate, job);

        if (existingSavedJob.isPresent()) {
            // Nếu đã lưu rồi -> Hủy lưu (Xóa khỏi DB)
            savedJobRepository.delete(existingSavedJob.get());
            return "Đã bỏ lưu công việc!";
        } else {
            // Nếu chưa lưu -> Lưu mới
            SavedJob newSavedJob = SavedJob.builder()
                    .candidate(candidate)
                    .job(job)
                    .build();
            savedJobRepository.save(newSavedJob);
            return "Đã lưu công việc thành công!";
        }
    }

    // 2. Lấy danh sách việc làm đã lưu (Sắp xếp theo thời gian lưu mới nhất)
    @Transactional(readOnly = true)
    public Page<JobResponse> getMySavedJobs(int page, int size) {
        User candidate = SecurityUtils.getCurrentUser();

        // Sắp xếp giảm dần theo trường savedAt (Lưu mới nhất nằm trên cùng)
        Pageable pageable = PageRequest.of(page, size, Sort.by("savedAt").descending());

        Page<SavedJob> savedJobs = savedJobRepository.findByCandidate(candidate, pageable);

        // Lấy Job từ SavedJob và chuyển sang DTO
        return savedJobs.map(savedJob -> {
            Job job = savedJob.getJob();
            JobResponse response = jobMapper.toResponse(job);
            // Nếu không có mapper, bạn tự dùng JobResponse.builder().id(job.getId())... như cũ nhé
            return response;
        });
    }

    // 3. (Optional) Check xem Job này đã lưu chưa để Frontend tô đỏ icon trái tim
    @Transactional(readOnly = true)
    public boolean checkIsSaved(Long jobId) {
        User candidate = SecurityUtils.getCurrentUser();
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Lỗi"));
        return savedJobRepository.existsByCandidateAndJob(candidate, job);
    }
}