package com.xuanthi.talentmatchingbe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j // Dùng cái này để ghi log lỗi nếu parse JSON thất bại
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    // BỔ SUNG: Dùng để biến List kỹ năng thành chuỗi JSON cho Python
    private final ObjectMapper objectMapper;

    @Transactional
    public JobResponse createJob(JobRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        // 1. Map sang Entity
        Job job = jobMapper.toEntity(request);

        // 2. Chế biến lương & JSON kỹ năng để lưu vào DB
        enrichJobWithAiAndSalaryLogic(job, request);

        job.setEmployer(currentUser);
        job.setStatus(JobStatus.OPEN);

        Job savedJob = jobRepository.save(job);

        // 3. Trả về Response (Lúc này @AfterMapping trong Mapper sẽ tự chạy)
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

        // 1. Dùng Mapper để đổ dữ liệu mới từ request vào job hiện tại
        jobMapper.updateJobFromDto(request, job);

        // 2. Cập nhật lại Logic AI (Vì HR có thể đã thay đổi kỹ năng yêu cầu)
        enrichJobWithAiAndSalaryLogic(job, request);

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

    // ==============================================================
    // HÀM HELPER: XỬ LÝ ĐẶC BIỆT CHO LÕI AI VÀ LƯƠNG THỎA THUẬN
    // ==============================================================
    private void enrichJobWithAiAndSalaryLogic(Job job, JobRequest request) {
        // 1. Xử lý Lương (Quét sạch min/max nếu là lương thỏa thuận)
        if (request.isSalaryNegotiable()) {
            job.setSalaryNegotiable(true);
            job.setSalaryMin(null);
            job.setSalaryMax(null);
        } else {
            job.setSalaryNegotiable(false);
            job.setSalaryMin(request.getSalaryMin());
            job.setSalaryMax(request.getSalaryMax());
        }

        // 2. Xử lý Kỹ năng cho Python AI (Gán trọng số 3 và 1)
        Map<String, Integer> aiSkillsMap = new HashMap<>();

        if (request.getMustHaveSkills() != null) {
            for (String skill : request.getMustHaveSkills()) {
                aiSkillsMap.put(skill.trim().toLowerCase(), 3);
            }
        }

        if (request.getNiceToHaveSkills() != null) {
            for (String skill : request.getNiceToHaveSkills()) {
                aiSkillsMap.putIfAbsent(skill.trim().toLowerCase(), 1); // Tránh ghi đè Must-have
            }
        }

        try {
            // Chuyển Map thành chuỗi JSON: {"java": 3, "docker": 1}
            job.setRequiredSkills(objectMapper.writeValueAsString(aiSkillsMap));

            // Chuyển List Danh mục thành chuỗi JSON: ["IT", "System Architect"]
            if (request.getCategories() != null) {
                job.setCategories(objectMapper.writeValueAsString(request.getCategories()));
            }
        } catch (JsonProcessingException e) {
            log.error("Lỗi parse JSON dữ liệu AI", e);
            throw new RuntimeException("Hệ thống gặp lỗi khi chuẩn hóa dữ liệu cho AI.");
        }
    }

    // Lấy chi tiết 1 Job
    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc!"));
        return jobMapper.toResponse(job);
        // Hàm toResponse này sẽ tự động chạy cái @AfterMapping để dịch JSON kỹ năng ra thành 2 List cho bạn!
    }

    // Lấy danh sách việc làm của 1 Công ty (Dùng cho trang Chi tiết công ty)
    @Transactional(readOnly = true)
    public Page<JobResponse> getPublicJobsByCompany(Long companyId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobs;

        if (keyword != null && !keyword.trim().isEmpty()) {
            jobs = jobRepository.searchActiveJobsByCompany(companyId, keyword.trim(), pageable);
        } else {
            jobs = jobRepository.findActiveJobsByCompany(companyId, pageable);
        }

        // jobMapper là class (hoặc code thủ công) chuyển từ Entity Job sang DTO JobResponse
        return jobs.map(jobMapper::toResponse);
    }
}