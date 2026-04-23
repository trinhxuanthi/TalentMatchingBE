package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.job.JobResponse;
import com.xuanthi.talentmatchingbe.entity.Job;
import com.xuanthi.talentmatchingbe.entity.SavedJob;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.mapper.JobMapper;
import com.xuanthi.talentmatchingbe.repository.JobRepository;
import com.xuanthi.talentmatchingbe.repository.SavedJobRepository;
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service xử lý chức năng lưu công việc của ứng viên
 * - Cho phép ứng viên lưu/bỏ lưu công việc
 * - Quản lý danh sách công việc đã lưu
 * - Kiểm tra trạng thái lưu của công việc
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    /**
     * Bật/tắt trạng thái lưu công việc
     * Nếu đã lưu thì bỏ lưu, nếu chưa lưu thì lưu mới
     *
     * @param jobId ID của công việc cần lưu/bỏ lưu
     * @return Thông báo kết quả thao tác
     * @throws IllegalArgumentException nếu jobId null hoặc job không tồn tại
     * @throws RuntimeException nếu user không được xác thực
     */
    @Transactional
    public String toggleSaveJob(Long jobId) {
        // Validation đầu vào
        if (jobId == null || jobId <= 0) {
            log.error("Job ID không hợp lệ: {}", jobId);
            throw new IllegalArgumentException("Job ID không được rỗng và phải lớn hơn 0!");
        }

        // Lấy thông tin user hiện tại
        User candidate = SecurityUtils.getCurrentUser();
        if (candidate == null || candidate.getId() == null) {
            log.error("Không thể xác thực người dùng hiện tại");
            throw new RuntimeException("Vui lòng đăng nhập để thực hiện thao tác này!");
        }

        log.info("User {} đang thực hiện toggle save job với ID: {}", candidate.getEmail(), jobId);

        // Kiểm tra job có tồn tại không
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy job với ID: {}", jobId);
                    return new RuntimeException("Công việc không tồn tại!");
                });

        // Kiểm tra trạng thái đã lưu
        Optional<SavedJob> existingSavedJob = savedJobRepository.findByCandidateAndJob(candidate, job);

        if (existingSavedJob.isPresent()) {
            // Nếu đã lưu rồi -> Hủy lưu (Xóa khỏi DB)
            savedJobRepository.delete(existingSavedJob.get());
            log.info("User {} đã bỏ lưu job: {}", candidate.getEmail(), job.getTitle());
            return "Đã bỏ lưu công việc!";
        } else {
            // Nếu chưa lưu -> Lưu mới
            SavedJob newSavedJob = SavedJob.builder()
                    .candidate(candidate)
                    .job(job)
                    .build();
            savedJobRepository.save(newSavedJob);
            log.info("User {} đã lưu job: {}", candidate.getEmail(), job.getTitle());
            return "Đã lưu công việc thành công!";
        }
    }

    /**
     * Lấy danh sách công việc đã lưu của ứng viên hiện tại
     * Sắp xếp theo thời gian lưu mới nhất
     *
     * @param page số trang (bắt đầu từ 0)
     * @param size số bản ghi mỗi trang
     * @return Page chứa danh sách JobResponse
     * @throws IllegalArgumentException nếu page < 0 hoặc size không hợp lệ
     * @throws RuntimeException nếu user không được xác thực
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> getMySavedJobs(int page, int size) {
        // Validation parameters
        if (page < 0) {
            log.error("Page number không hợp lệ: {}", page);
            throw new IllegalArgumentException("Số trang phải >= 0!");
        }
        if (size <= 0 || size > 100) {
            log.error("Page size không hợp lệ: {}", size);
            throw new IllegalArgumentException("Kích thước trang phải từ 1-100!");
        }

        // Lấy thông tin user hiện tại
        User candidate = SecurityUtils.getCurrentUser();
        if (candidate == null || candidate.getId() == null) {
            log.error("Không thể xác thực người dùng hiện tại");
            throw new RuntimeException("Vui lòng đăng nhập để thực hiện thao tác này!");
        }

        log.debug("User {} đang lấy danh sách saved jobs - page: {}, size: {}", candidate.getEmail(), page, size);

        // Sắp xếp giảm dần theo trường savedAt (Lưu mới nhất nằm trên cùng)
        Pageable pageable = PageRequest.of(page, size, Sort.by("savedAt").descending());

        Page<SavedJob> savedJobs = savedJobRepository.findByCandidate(candidate, pageable);

        log.info("User {} có {} saved jobs, trả về trang {} với {} items",
                candidate.getEmail(), savedJobs.getTotalElements(), page, savedJobs.getNumberOfElements());

        // Chuyển đổi sang DTO
        return savedJobs.map(savedJob -> {
            Job job = savedJob.getJob();
            JobResponse response = jobMapper.toResponse(job);
            return response;
        });
    }

    /**
     * Kiểm tra xem công việc đã được lưu bởi ứng viên hiện tại chưa
     * Sử dụng để hiển thị trạng thái icon trái tim trên frontend
     *
     * @param jobId ID của công việc cần kiểm tra
     * @return true nếu đã lưu, false nếu chưa lưu
     * @throws IllegalArgumentException nếu jobId null hoặc job không tồn tại
     * @throws RuntimeException nếu user không được xác thực
     */
    @Transactional(readOnly = true)
    public boolean checkIsSaved(Long jobId) {
        // Validation đầu vào
        if (jobId == null || jobId <= 0) {
            log.error("Job ID không hợp lệ: {}", jobId);
            throw new IllegalArgumentException("Job ID không được rỗng và phải lớn hơn 0!");
        }

        // Lấy thông tin user hiện tại
        User candidate = SecurityUtils.getCurrentUser();
        if (candidate == null || candidate.getId() == null) {
            log.error("Không thể xác thực người dùng hiện tại");
            throw new RuntimeException("Vui lòng đăng nhập để thực hiện thao tác này!");
        }

        // Kiểm tra job có tồn tại không
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy job với ID: {}", jobId);
                    return new RuntimeException("Công việc không tồn tại!");
                });

        boolean isSaved = savedJobRepository.existsByCandidateAndJob(candidate, job);
        log.debug("User {} check saved status for job {}: {}", candidate.getEmail(), jobId, isSaved);

        return isSaved;
    }
}