package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.job.JobRequest;
import com.xuanthi.talentmatchingbe.dto.job.JobResponse;
import com.xuanthi.talentmatchingbe.entity.Job;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.enums.JobType;
import com.xuanthi.talentmatchingbe.mapper.JobMapper;
import com.xuanthi.talentmatchingbe.repository.JobRepository;
import com.xuanthi.talentmatchingbe.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    @Transactional
    public JobResponse createJob(JobRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        // 1. MapStruct tự động bê toàn bộ dữ liệu (kể cả List Kỹ năng) từ Request sang Entity
        Job job = jobMapper.toEntity(request);

        // 2. Chế biến logic lương
        applySalaryLogic(job);

        // 3. Set quyền sở hữu và trạng thái (Mặc định là OPEN)
        job.setEmployer(currentUser);
        job.setStatus(JobStatus.OPEN);

        // 4. Lưu DB (Converter tự biến List Skills thành chuỗi JSON/phẩy cất xuống DB)
        Job savedJob = jobRepository.save(job);

        return jobMapper.toResponse(savedJob);
    }

    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request) {
        User currentEmployer = SecurityUtils.getCurrentUser();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc!"));

        if (!job.getEmployer().getId().equals(currentEmployer.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài đăng này!");
        }

        // 1. Dùng MapStruct ghi đè dữ liệu mới vào Entity hiện tại (Status vẫn được giữ nguyên do @Mapping ignore)
        jobMapper.updateJobFromDto(request, job);

        // 2. Cập nhật lại Logic Lương
        applySalaryLogic(job);

        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc với ID: " + jobId));

        // Soft Delete: Chuyển trạng thái sang DELETED thay vì xóa hẳn dòng trong DB
        job.setStatus(JobStatus.DELETED);

        jobRepository.save(job);
    }

    public Page<JobResponse> getAllPublicJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return jobRepository.findAllByStatus(JobStatus.OPEN, pageable)
                .map(jobMapper::toResponse);
    }

    public Page<JobResponse> getMyJobs(int page, int size) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Bạn cần đăng nhập để xem danh sách bài đăng!");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return jobRepository.findAllByEmployerId(currentUser.getId(), pageable)
                .map(jobMapper::toResponse);
    }

    public Page<JobResponse> searchJobs(String title, String location, JobType jobType,
                                        BigDecimal minSalary, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Job> jobPage = jobRepository.findJobsWithFilters(title, location, jobType, minSalary, pageable);
        return jobPage.map(jobMapper::toResponse);
    }

    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc!"));
        return jobMapper.toResponse(job);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getPublicJobsByCompany(Long companyId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobs;

        // 🔥 Đã đổi tên hàm query để phản ánh đúng việc sử dụng JobStatus.OPEN
        if (keyword != null && !keyword.trim().isEmpty()) {
            jobs = jobRepository.searchOpenJobsByCompany(companyId, keyword.trim(), pageable);
        } else {
            jobs = jobRepository.findOpenJobsByCompany(companyId, pageable);
        }

        return jobs.map(jobMapper::toResponse);
    }

    // ==============================================================
    // HÀM HELPER: XỬ LÝ ĐẶC BIỆT CHO LƯƠNG
    // ==============================================================
    private void applySalaryLogic(Job job) {
        // Quét sạch min/max nếu là lương thỏa thuận
        if (job.isSalaryNegotiable()) {
            job.setSalaryMin(null);
            job.setSalaryMax(null);
        }
    }
}