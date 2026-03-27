package com.xuanthi.talentmatchingbe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanthi.talentmatchingbe.dto.application.ApplicationResponse;
import com.xuanthi.talentmatchingbe.dto.application.CandidateDashboardResponse;
import com.xuanthi.talentmatchingbe.dto.application.EmployerDashboardResponse;
import com.xuanthi.talentmatchingbe.dto.application.MonthlyStatResponse;
import com.xuanthi.talentmatchingbe.entity.Application;
import com.xuanthi.talentmatchingbe.entity.Job;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.ApplicationStatus;
import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.mapper.ApplicationMapper;
import com.xuanthi.talentmatchingbe.repository.ApplicationRepository;
import com.xuanthi.talentmatchingbe.repository.JobRepository;
import com.xuanthi.talentmatchingbe.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final NotificationService notificationService;
    private final SkillAliasService skillAliasService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    private final String PYTHON_AI_URL = "http://localhost:5000/api/match-cv";

    // ==============================================================
    // 1. LUỒNG NỘP ĐƠN (Lọc Cứng & Chuyển Trạng Thái)
    // ==============================================================
    @Transactional

    // ==============================================================
    // 2. GỌI PYTHON AI (Xử lý Bất đồng bộ)
    // ==============================================================
    @Async
    public void callPythonAiToEvaluate(Long applicationId, Long jobId) {
        log.info("⏳ [AI] Chuẩn bị gọi Python Matcher cho Đơn ID: {}", applicationId);

        Application app = applicationRepository.findById(applicationId).orElse(null);
        Job job = jobRepository.findById(jobId).orElse(null);

        if (app == null || job == null) return;

        try {
            app.setIsAiScored(1); // Trạng thái: Đang chấm
            applicationRepository.save(app);

            // 1. Lấy Từ điển động từ DB (Skill Aliases)
            Map<String, String> dynamicAliases = skillAliasService.getDynamicAliasesMap();

            // 2. Chuẩn bị Payload
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("cv_url", app.getCvUrl());
            requestPayload.put("job_requirements", job.getDescription() + " " + job.getRequirements());
            requestPayload.put("job_skills_json", job.getRequiredSkills());
            requestPayload.put("dynamic_aliases", dynamicAliases); // Gửi từ điển sang Python

            log.info("🚀 [AI] Bắn Request sang Python. Tệp CV: {}", app.getCvUrl());
            ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_AI_URL, requestPayload, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> aiResult = response.getBody();

                // Lấy điểm
                Double score = Double.valueOf(aiResult.get("match_score").toString());
                // Chuyển Object phân tích thành chuỗi JSON
                String analysisJsonString = objectMapper.writeValueAsString(aiResult.get("ai_analysis"));

                app.setMatchScore(score);
                app.setAiAnalysis(analysisJsonString);
                app.setIsAiScored(2); // Trạng thái: Đã chấm xong

                applicationRepository.save(app);
                log.info("✅ [AI] Thành công! CV ID {} đạt {} điểm.", app.getId(), score);
            } else {
                throw new RuntimeException("Python API trả về lỗi: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ [AI] Lỗi khi kết nối Python AI: {}", e.getMessage());
            app.setIsAiScored(-1); // Trạng thái: Lỗi
            applicationRepository.save(app);
        }
    }

    // ==============================================================
    // 3. QUẢN LÝ ĐƠN ỨNG TUYỂN
    // ==============================================================
    @Transactional
    public void updateStatus(Long appId, ApplicationStatus status, String notes) {
        User currentEmployer = SecurityUtils.getCurrentUser();
        Application app = applicationRepository.findByIdAndJobEmployerId(appId, currentEmployer.getId())
                .orElseThrow(() -> new RuntimeException("Bạn không có quyền xử lý đơn này!"));

        app.setStatus(status);
        if (notes != null) app.setNotes(notes);
        applicationRepository.save(app);

        String statusVi = switch (status) {
            case REVIEWING -> "đang được xem xét";
            case INTERVIEW -> "được mời phỏng vấn";
            case ACCEPTED -> "đã được chấp nhận";
            case REJECTED -> "đã bị từ chối";
            default -> status.toString();
        };

        notificationService.sendNotification(
                app.getCandidate().getId(),
                currentEmployer.getId(),
                "Cập nhật trạng thái ứng tuyển",
                "Đơn ứng tuyển vào vị trí [" + app.getJob().getTitle() + "] của bạn " + statusVi + ".",
                "APPLICATION",
                app.getId()
        );
    }

    public Page<ApplicationResponse> getApplicationsByJob(Long jobId, int page, int size) {
        User currentEmployer = SecurityUtils.getCurrentUser();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng!"));

        if (!job.getEmployer().getId().equals(currentEmployer.getId())) {
            throw new RuntimeException("Bạn không có quyền truy cập danh sách này!");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("matchScore").descending());
        return applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId, pageable)
                .map(applicationMapper::toResponse);
    }

    public Page<ApplicationResponse> getMyApplications(int page, int size) {
        User currentUser = SecurityUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        return applicationRepository.findByCandidateIdOrderByAppliedAtDesc(currentUser.getId(), pageable)
                .map(applicationMapper::toResponse);
    }

    @Transactional
    public ApplicationResponse getApplicationDetail(Long appId) {
        User currentEmployer = SecurityUtils.getCurrentUser();
        Application app = applicationRepository.findByIdAndJobEmployerId(appId, currentEmployer.getId())
                .orElseThrow(() -> new RuntimeException("Đơn không tồn tại hoặc bạn không có quyền!"));

        if (!app.isViewed()) {
            app.setViewed(true);
            applicationRepository.save(app);
        }
        return applicationMapper.toResponse(app);
    }




    // ==============================================================
    // 5. THỐNG KÊ DASHBOARD (Giữ nguyên logic của bạn)
    // ==============================================================
    public CandidateDashboardResponse getCandidateStats() {
        Long userId = SecurityUtils.getCurrentUser().getId();

        return CandidateDashboardResponse.builder()
                .totalApplied(applicationRepository.countByCandidateId(userId))
                .pendingCount(applicationRepository.countByCandidateIdAndStatus(userId, ApplicationStatus.PENDING))
                .interviewCount(applicationRepository.countByCandidateIdAndStatus(userId, ApplicationStatus.INTERVIEW))
                .acceptedCount(applicationRepository.countByCandidateIdAndStatus(userId, ApplicationStatus.ACCEPTED))
                .rejectedCount(applicationRepository.countByCandidateIdAndStatus(userId, ApplicationStatus.REJECTED))
                .build();
    }

    public EmployerDashboardResponse getEmployerStats() {
        Long employerId = SecurityUtils.getCurrentUser().getId();

        long activeJobs = jobRepository.countByEmployerIdAndStatus(employerId, JobStatus.OPEN);
        long totalApps = applicationRepository.countByJobEmployerId(employerId);
        long pendingApps = applicationRepository.countByJobEmployerIdAndStatus(employerId, ApplicationStatus.PENDING);
        long interviewApps = applicationRepository.countByJobEmployerIdAndStatus(employerId, ApplicationStatus.INTERVIEW);
        long unreadApps = applicationRepository.countByJobEmployerIdAndIsViewedFalse(employerId);

        List<Job> myJobs = jobRepository.findAllByEmployerId(employerId);
        Map<String, Long> statsPerJob = myJobs.stream()
                .collect(Collectors.toMap(
                        Job::getTitle,
                        job -> applicationRepository.countByJobId(job.getId()),
                        (existing, replacement) -> existing
                ));

        return EmployerDashboardResponse.builder()
                .totalActiveJobs(activeJobs)
                .totalApplications(totalApps)
                .pendingApplications(pendingApps)
                .unreadApplications(unreadApps)
                .interviewScheduled(interviewApps)
                .applicationsPerJob(statsPerJob)
                .build();
    }

    public List<MonthlyStatResponse> getMonthlyStats() {
        Long employerId = SecurityUtils.getCurrentUser().getId();
        List<Object[]> results = applicationRepository.getMonthlyStatsNative(employerId);

        return results.stream()
                .map(result -> new MonthlyStatResponse(
                        (String) result[0],
                        ((Number) result[1]).longValue()
                ))
                .collect(Collectors.toList());
    }




}