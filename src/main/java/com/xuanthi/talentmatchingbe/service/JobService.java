package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.job.JobRequest;
import com.xuanthi.talentmatchingbe.dto.job.JobResponse;
import com.xuanthi.talentmatchingbe.entity.AiJobSetting;
import com.xuanthi.talentmatchingbe.entity.Job;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.enums.JobType;
import com.xuanthi.talentmatchingbe.mapper.JobMapper;
import com.xuanthi.talentmatchingbe.repository.JobRepository;
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.xuanthi.talentmatchingbe.enums.ProFeature;

import java.math.BigDecimal;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final ProService proService;

    @Transactional
    public JobResponse createJob(JobRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        if (currentUser == null) {
            log.warn("Unauthorized job creation attempt");
            throw new RuntimeException("Vui lòng đăng nhập trước!");
        }

        if (request == null) {
            log.error("Job request is null");
            throw new RuntimeException("Dữ liệu công việc không hợp lệ!");
        }

        // 1. KIỂM TRA GIỚI HẠN BÀI ĐĂNG
        if (!currentUser.isPro()) {
            // Đếm xem HR này đã đăng bao nhiêu bài rồi
            long jobCount = jobRepository.countByEmployerId(currentUser.getId());

            if (jobCount >= 5) {
                // Đã chạm limit -> Đuổi ra
                throw new AccessDeniedException("Gói BASIC chỉ được đăng tối đa 5 tin tuyển dụng. Vui lòng nâng cấp PRO để đăng không giới hạn!");
            }
        } else {
            // 2. NẾU LÀ PRO: Vượt qua mốc 5 bài thì bắt đầu ghi Log là họ đang dùng đặc quyền PRO
            long jobCount = jobRepository.countByEmployerId(currentUser.getId());
            if (jobCount >= 5) {
                proService.validateAndLogUsage(currentUser, ProFeature.UNLIMITED_JOB_POST);
            }
        }
        log.info("User {} creating new job: {}", currentUser.getEmail(), request.getTitle());

        Job job = jobMapper.toEntity(request);

        // ✅ THÊM MỚI: Tự động khởi tạo AI Setting nếu HR không cung cấp
        if (job.getAiJobSetting() == null) {
            job.setAiJobSetting(new AiJobSetting()); // Gọi default constructor chứa các số 0.3, 0.1...
        }

        applySalaryLogic(job);

        job.setEmployer(currentUser);
        job.setPriority(currentUser.isPro());
        job.setStatus(JobStatus.OPEN);

        Job savedJob = jobRepository.save(job);
        log.info("Job created successfully with ID: {} by user: {}", savedJob.getId(), currentUser.getEmail());

        return jobMapper.toResponse(savedJob);
    }

    @Transactional // Sếp nhớ thêm @Transactional để đảm bảo dữ liệu lưu xuống đồng bộ
    public JobResponse updateJob(Long jobId, JobRequest request) {
        User currentEmployer = SecurityUtils.getRequiredCurrentUser();

        // 1. Tìm Job cũ
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công việc với ID: " + jobId));

        // 2. Check quyền (Chỉ chủ bài đăng mới được sửa)
        if (!job.getEmployer().getId().equals(currentEmployer.getId())) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa bài đăng này!");
        }

        // 3. Gọi Mapper (Lưu ý: MapStruct lúc này đã được ignore 2 cái List như em dặn ở trên)
        jobMapper.updateJobFromDto(request, job);

        // 4. ✅ XỬ LÝ THỦ CÔNG 2 LIST (Né lỗi UnsupportedOperationException)
        // Mình tạo mới hoàn toàn ArrayList từ dữ liệu Request gửi lên
        if (request.getRequiredSkills() != null) {
            job.setRequiredSkills(new ArrayList<>(request.getRequiredSkills()));
        }

        if (request.getCategories() != null) {
            job.setCategories(new ArrayList<>(request.getCategories()));
        }

        // 5. ✅ XỬ LÝ AI SETTING (Giữ nguyên quan hệ 1-1)
        if (request.getAiSettings() != null) {
            if (job.getAiJobSetting() == null) {
                job.setAiJobSetting(new AiJobSetting());
            }
            // Map các giá trị từ DTO aiSettings vào Entity
            AiJobSetting ai = job.getAiJobSetting();
            ai.setWeightExp(request.getAiSettings().getWeightExp());
            ai.setWeightSkills(request.getAiSettings().getWeightSkills());
            ai.setWeightRole(request.getAiSettings().getWeightRole());
            ai.setWeightTools(request.getAiSettings().getWeightTools());
            ai.setWeightEdu(request.getAiSettings().getWeightEdu());
            ai.setWeightSoft(request.getAiSettings().getWeightSoft());
        }

        // 6. Logic lương và lưu trữ
        applySalaryLogic(job);
        job.setPriority(currentEmployer.isPro());
        Job updatedJob = jobRepository.save(job);
        log.info("Employer ID {} đã cập nhật thành công Job ID {}", currentEmployer.getId(), jobId);

        return jobMapper.toResponse(updatedJob);
    }

    @Transactional
    public void deleteJob(Long jobId) {
        log.info("Soft deleting job ID: {}", jobId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.error("Job not found for deletion: {}", jobId);
                    return new RuntimeException("Không tìm thấy công việc với ID: " + jobId);
                });

        // Soft delete: chuyển trạng thái thành DELETED
        job.setStatus(JobStatus.DELETED);
        jobRepository.save(job);
        
        log.info("Job {} soft deleted successfully", jobId);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getAllPublicJobs(int page, int size) {
        log.debug("Fetching all public jobs - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return jobRepository.findAllByStatus(JobStatus.OPEN, pageable)
                .map(jobMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getMyJobs(int page, int size) {
        User currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null) {
            log.warn("Unauthorized access attempt to my jobs");
            throw new RuntimeException("Bạn cần đăng nhập để xem danh sách bài đăng!");
        }

        log.debug("Fetching jobs for user {} - page: {}, size: {}", currentUser.getEmail(), page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return jobRepository.findAllByEmployerId(currentUser.getId(), pageable)
                .map(jobMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String title, String location, JobType jobType,
                                        BigDecimal minSalary, int page, int size) {
        log.debug("Searching jobs - title: {}, location: {}, jobType: {}, minSalary: {}", 
                title, location, jobType, minSalary);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Job> jobPage = jobRepository.findJobsWithFilters(title, location, jobType, minSalary, pageable);
        
        log.info("Found {} jobs matching search criteria", jobPage.getTotalElements());
        return jobPage.map(jobMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId) {
        log.debug("Fetching job detail for ID: {}", jobId);
        
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.warn("Job not found: {}", jobId);
                    return new RuntimeException("Không tìm thấy công việc!");
                });

        log.info("Job {} retrieved successfully", jobId);
        return jobMapper.toResponse(job);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getPublicJobsByCompany(Long companyId, String keyword, int page, int size) {
        log.debug("Fetching jobs for company ID: {} - keyword: {}, page: {}", companyId, keyword, page);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobs;

        if (StringUtils.hasText(keyword)) {
            log.debug("Searching jobs by keyword: {}", keyword);
            jobs = jobRepository.searchOpenJobsByCompany(companyId, keyword.trim(), pageable);
        } else {
            jobs = jobRepository.findOpenJobsByCompany(companyId, pageable);
        }

        log.info("Found {} open jobs for company {}", jobs.getTotalElements(), companyId);
        return jobs.map(jobMapper::toResponse);
    }

    // ==============================================================
    // HÀM HELPER: XỬ LÝ ĐẶC BIỆT CHO LƯƠNG
    // ==============================================================
    private void applySalaryLogic(Job job) {
        // Nếu lương thỏa thuận, clear min/max
        if (job.isSalaryNegotiable()) {
            log.debug("Job salary is negotiable, clearing min/max salary");
            job.setSalaryMin(null);
            job.setSalaryMax(null);
        }
    }
}