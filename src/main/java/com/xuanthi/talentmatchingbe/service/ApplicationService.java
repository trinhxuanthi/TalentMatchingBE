package com.xuanthi.talentmatchingbe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanthi.talentmatchingbe.dto.application.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    private final CloudinaryService cloudinaryService;
    private final HardRuleValidator hardRuleValidator;

    @Lazy
    @Autowired
    private ApplicationService self;

    private final String PYTHON_AI_URL = "http://localhost:5000/api/quick-match";

    // ==============================================================
    // 1. LUỒNG NỘP ĐƠN (Lọc Cứng & Chuyển Trạng Thái)
    // ==============================================================
    @Transactional
    public ApplicationResponse applyForJob(CandidateApplyRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Công việc không tồn tại hoặc đã bị xóa!"));

        Application application = applicationMapper.toEntity(request);
        application.setJob(job);
        if (currentUser != null) {
            application.setCandidate(currentUser);
        }

        // Kích hoạt máy chém
        HardRuleValidator.ValidationResult validation = hardRuleValidator.checkHardRules(request, job);

        if (!validation.isValid()) {
            application.setStatus(ApplicationStatus.REJECTED);
            application.setMatchScore(0);
            application.setNotes("Hệ thống tự động loại: " + validation.failReason());
            application.setIsAiScored(2);

            Application savedApp = applicationRepository.save(application);
            log.warn("[He Thong] CV bi loai tu vong gui xe. Ly do: {}", validation.failReason());
            return applicationMapper.toResponse(savedApp);
        }

        application.setStatus(ApplicationStatus.PENDING);
        application.setIsAiScored(0);
        Application savedApp = applicationRepository.save(application);

        // Gọi bất đồng bộ Python AI
        self.callPythonAiToEvaluate(savedApp.getId(), job.getId());

        return applicationMapper.toResponse(savedApp);
    }

    // ==============================================================
    // 2. GỌI PYTHON AI (Xử lý Bất đồng bộ cho ứng viên)
    // ==============================================================

    // Nho giu lai @Async de ham chay ngam, khong lam treo Frontend cua ung vien
    @Async // Nhớ giữ nguyên @Async nếu bro đang cho nó chạy ngầm
    public void callPythonAiToEvaluate(Long applicationId, Long jobId) {
        log.info("[AI] Chuan bi goi Python Matcher cho Don ID: {}", applicationId);

        Application app = applicationRepository.findById(applicationId).orElse(null);
        Job job = jobRepository.findById(jobId).orElse(null);

        if (app == null || job == null) return;

        try {
            app.setIsAiScored(1);
            applicationRepository.save(app);

            // ĐÃ DỌN DẸP: Xóa sạch logic của skillAliasService
            // Giờ chỉ truyền đúng Kỹ năng bắt buộc từ Job sang cho AI chấm
            String customRules = "Kỹ năng bắt buộc: " +
                    (job.getRequiredSkills() != null ? String.join(", ", job.getRequiredSkills()) : "");

            // Dong goi du lieu chuan Form-Data cho Python
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("cv_urls", app.getCvUrl());
            body.add("jd_text", job.getDescription() + " \n " + job.getRequirements());
            body.add("custom_rules", customRules);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("[AI] Ban Request sang Python. Tep CV: {}", app.getCvUrl());
            ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_AI_URL, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> aiResult = response.getBody();

                // 1. Cap nhat Diem so
                if (aiResult.get("match_score") != null) {
                    Double rawScore = Double.valueOf(aiResult.get("match_score").toString());
                    app.setMatchScore(rawScore.intValue());
                }

                // 2. BOC LOT: Cap nhat De xuat (Recommendation)
                if (aiResult.get("recommendation") != null) {
                    // Neu Python tra ve tu vong loc nhanh (VD: "REJECTED")
                    app.setAiRecommendation((String) aiResult.get("recommendation"));
                } else {
                    // Neu Python khong tra ve, Java tu dong xep loai theo diem
                    int score = app.getMatchScore() != null ? app.getMatchScore() : 0;
                    if (score >= 75) {
                        app.setAiRecommendation("ĐỀ XUẤT"); // Diem cao -> Nhan su nen goi dien
                    } else if (score >= 50) {
                        app.setAiRecommendation("XEM XÉT"); // Diem trung binh -> Doc ky lai CV
                    } else {
                        app.setAiRecommendation("TỪ CHỐI"); // Diem thap -> Loai luon
                    }
                }

                // 3. Dong goi JSON phan tich chi tiet an toan bang HashMap
                Map<String, Object> analysisMap = new java.util.HashMap<>();
                analysisMap.put("jd_core_evaluation", aiResult.get("jd_core_evaluation"));
                analysisMap.put("custom_rules_evaluation", aiResult.get("custom_rules_evaluation"));
                analysisMap.put("executive_summary", aiResult.get("executive_summary"));

                // Neu bi loai nhanh tu vong gui xe, Python se tra ve truong ai_analysis
                if (aiResult.get("ai_analysis") != null) {
                    analysisMap.put("ai_analysis", aiResult.get("ai_analysis"));
                }

                // Ep toan bo sang chuoi JSON roi luu vao cot LONGTEXT
                String analysisJsonString = objectMapper.writeValueAsString(analysisMap);
                app.setAiAnalysis(analysisJsonString);

                app.setIsAiScored(2); // 2: Cham xong

                applicationRepository.save(app);
                log.info("[AI] Thanh cong! CV ID {} dat {} diem - Xep loai: {}", app.getId(), app.getMatchScore(), app.getAiRecommendation());
            } else {
                throw new RuntimeException("Python API tra ve loi: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("[AI] Loi khi ket noi Python AI: {}", e.getMessage());
            app.setIsAiScored(-1);
            applicationRepository.save(app);
        }
    }


    // ==============================================================
    // 4. QUẢN LÝ ĐƠN ỨNG TUYỂN VÀ XẾP HẠNG
    // ==============================================================
    // Đổi ApplicationResponse thành ApplicationSimpleResponse
    public Page<ApplicationSimpleResponse> getApplicationsByJob(Long jobId, int page, int size) {
        User currentEmployer = SecurityUtils.getCurrentUser();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng!"));

        if (!job.getEmployer().getId().equals(currentEmployer.getId())) {
            throw new RuntimeException("Bạn không có quyền truy cập danh sách này!");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("matchScore").descending());

        // Đổi từ applicationMapper::toResponse sang applicationMapper::toSimpleResponse
        return applicationRepository.findByJobId(jobId, pageable)
                .map(applicationMapper::toSimpleResponse);
    }

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
    // 5. THỐNG KÊ DASHBOARD
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