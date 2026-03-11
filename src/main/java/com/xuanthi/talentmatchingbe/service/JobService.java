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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    @Transactional
    public JobResponse createJob(JobRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Bạn cần đăng nhập để thực hiện hành động này!");
        }
        // Chuyển DTO sang Entity thông qua Mapper
        Job job = jobMapper.toEntity(request);

        // Thiết lập các thông số mặc định của hệ thống
        job.setEmployer(currentUser);
        job.setStatus(JobStatus.OPEN);

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
        // Dùng Mapper để đổ dữ liệu mới từ request vào job hiện tại
        jobMapper.updateJobFromDto(request, job);
        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng!"));

        // Kiểm tra quyền sở hữu
        User currentUser = SecurityUtils.getCurrentUser();
        if (!job.getEmployer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa bài đăng này!");
        }

        jobRepository.delete(job);
    }

    // Lấy Job công khai cho Ứng viên
    public Page<JobResponse> getAllPublicJobs(int page, int size) {
        // Sắp xếp bài mới nhất lên đầu để tối ưu trải nghiệm người dùng
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return jobRepository.findAllByStatus(JobStatus.OPEN, pageable)
                .map(jobMapper::toResponse);
    }

    // Lấy Job của chính chủ cho Employer
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
        // Sắp xếp: Luôn ưu tiên bài đăng mới nhất hiện lên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Gọi Repository với các filter đã tối ưu Index
        Page<Job> jobPage = jobRepository.findJobsWithFilters(title, location, jobType, minSalary, pageable);
        return jobPage.map(jobMapper::toResponse);
    }
}
