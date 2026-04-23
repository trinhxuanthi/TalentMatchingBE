package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.admin.AdminJobResponse;
import com.xuanthi.talentmatchingbe.entity.Job;
import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.repository.ApplicationRepository;
import com.xuanthi.talentmatchingbe.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminJobService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    // 1. Lấy danh sách Job có phân trang và tìm kiếm
    public Page<AdminJobResponse> getAllJobs(String keyword, JobStatus status, int page, int size) {
        return jobRepository.findAllForAdmin(keyword, status, PageRequest.of(page, size))
                .map(this::mapToAdminResponse);
    }

    // 2. Thay đổi trạng thái bài đăng (Dùng để khóa bài vi phạm)
    @Transactional
    public void updateJobStatus(Long jobId, JobStatus newStatus) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng tuyển dụng"));
        job.setStatus(newStatus);
        jobRepository.save(job);
    }

    // 3. Xóa vĩnh viễn (Chỉ dùng khi bài đăng là rác/spam)
    @Transactional
    public void deleteJob(Long jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new RuntimeException("Không tìm thấy bài đăng để xóa");
        }
        jobRepository.deleteById(jobId);
    }

    private AdminJobResponse mapToAdminResponse(Job job) {
        return AdminJobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getEmployer().getFullName())
                .employerEmail(job.getEmployer().getEmail())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .totalApplications(applicationRepository.countByJobId(job.getId()))
                .build();
    }
}