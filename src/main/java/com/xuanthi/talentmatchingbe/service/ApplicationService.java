package com.xuanthi.talentmatchingbe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanthi.talentmatchingbe.dto.application.*;
import com.xuanthi.talentmatchingbe.entity.AiJobSetting;
import com.xuanthi.talentmatchingbe.entity.Application;
import com.xuanthi.talentmatchingbe.entity.Job;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.ApplicationStatus;
import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.mapper.ApplicationMapper;
import com.xuanthi.talentmatchingbe.repository.ApplicationRepository;
import com.xuanthi.talentmatchingbe.repository.JobRepository;
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final AiSettingService aiSettingService;
    private final HardRuleValidator hardRuleValidator;
    private final MailService mailService;

    @Lazy
    @Autowired
    private ApplicationService self;

    @Value("${python.ai.url:http://localhost:5000/api/quick-match}")
    private String pythonAiUrl;

    // ==============================================================
    // 1. LUỒNG NỘP ĐƠN (Lọc Cứng & Chuyển Trạng Thái)
    // ==============================================================
    @Transactional
    public ApplicationResponse applyForJob(CandidateApplyRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found or deleted!"));



        Application application = applicationMapper.toEntity(request);
        application.setJob(job);
        if (currentUser != null) {
            application.setCandidate(currentUser);
        }

        String applyEmail = application.getEmail();

        if (applicationRepository.existsByJobIdAndEmail(job.getId(), applyEmail)) {
            log.warn("[Anti-Spam] User {} tried to duplicate apply for job {}", applyEmail, job.getId());
            throw new RuntimeException("Bạn đã ứng tuyển vị trí này rồi. Vui lòng chờ phản hồi từ HR!");
        }

        // Kích hoạt máy chém
        HardRuleValidator.ValidationResult validation = hardRuleValidator.checkHardRules(request, job);

        if (!validation.isValid()) {
            application.setStatus(ApplicationStatus.REJECTED);
            application.setMatchScore(0);
            application.setNotes("Auto-rejected by system: " + validation.failReason());
            application.setIsAiScored(2);

            Application savedApp = applicationRepository.save(application);
            log.warn("[System] CV auto-rejected for job {} by user {}: {}", job.getId(), currentUser != null ? currentUser.getEmail() : "unknown", validation.failReason());
            return applicationMapper.toResponse(savedApp);
        }

        application.setStatus(ApplicationStatus.PENDING);
        application.setIsAiScored(0);
        Application savedApp = applicationRepository.save(application);
        log.info("Application submitted for job {} by user {}", job.getId(), currentUser != null ? currentUser.getEmail() : "unknown");

        // Gọi bất đồng bộ Python AI
        self.callPythonAiToEvaluate(savedApp.getId(), job.getId());

        return applicationMapper.toResponse(savedApp);
    }

    // ==============================================================
    // 2. GỌI PYTHON AI (Xử lý Bất đồng bộ cho ứng viên)
    // ==============================================================

    // Nho giu lai @Async de ham chay ngam, khong lam treo Frontend cua ung vien
    @Async
    public void callPythonAiToEvaluate(Long applicationId, Long jobId) {
        log.info("[AI] Preparing to call Python Matcher for Application ID: {}", applicationId);

        Application app = applicationRepository.findById(applicationId).orElse(null);
        Job job = jobRepository.findById(jobId).orElse(null);

        if (app == null || job == null) {
            log.warn("[AI] Application or Job not found, skipping evaluation");
            return;
        }

        // ===== RETRY CONFIGURATION =====
        final int MAX_RETRIES = 3;
        final long INITIAL_DELAY_MS = 2000; // 2 seconds
        final long MAX_DELAY_MS = 30000; // 30 seconds

        int attempt = 0;
        boolean success = false;

        while (attempt < MAX_RETRIES && !success) {
            attempt++;
            long delayMs = Math.min(INITIAL_DELAY_MS * (long) Math.pow(2, attempt - 1), MAX_DELAY_MS);

            try {
                log.info("[AI] Attempt {}/{} for Application ID: {}", attempt, MAX_RETRIES, applicationId);

                // Set status to processing on first attempt
                if (attempt == 1) {
                    app.setIsAiScored(1);
                    applicationRepository.save(app);
                }

                // ĐÃ DỌN DẸP: Xóa sạch logic của skillAliasService
                // Giờ chỉ truyền đúng Kỹ năng bắt buộc từ Job sang cho AI chấm
                String customRules = buildCustomRules(job);

                // Dong goi du lieu chuan Form-Data cho Python
                MultiValueMap<String, Object> body = buildRequestBody(app, job, customRules);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

                log.info("[AI] Sending request to Python. CV URL: {}", app.getCvUrl());
                ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(pythonAiUrl, requestEntity, (Class<Map<String, Object>>) (Class<?>) Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    processAiResponse(app, response.getBody());
                    applicationRepository.save(app);
                    log.info("[AI] Success! CV ID {} scored {} - Recommendation: {}", app.getId(), app.getMatchScore(), app.getAiRecommendation());
                    success = true;
                } else {
                    throw new RuntimeException("Python API returned error: " + response.getStatusCode());
                }

            } catch (Exception e) {
                log.warn("[AI] Attempt {}/{} failed for Application ID {}: {}", attempt, MAX_RETRIES, applicationId, e.getMessage());

                if (attempt < MAX_RETRIES) {
                    log.info("[AI] Retrying in {} ms for Application ID: {}", delayMs, applicationId);
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("[AI] Retry interrupted for Application ID: {}", applicationId);
                        break;
                    }
                } else {
                    log.error("[AI] All {} attempts failed for Application ID: {}. Final error: {}", MAX_RETRIES, applicationId, e.getMessage());
                    app.setIsAiScored(-1); // Mark as failed
                    app.setNotes("AI evaluation failed after " + MAX_RETRIES + " attempts: " + e.getMessage());
                    applicationRepository.save(app);
                }
            }
        }

        if (!success) {
            log.error("[AI] Failed to evaluate Application ID: {} after {} attempts", applicationId, MAX_RETRIES);
        }
    }

    private String buildCustomRules(Job job) {
        return "Required Skills: " + (job.getRequiredSkills() != null ? String.join(", ", job.getRequiredSkills()) : "");
    }

    private MultiValueMap<String, Object> buildRequestBody(Application app, Job job, String customRules) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("cv_urls", app.getCvUrl());
        body.add("jd_text", job.getDescription() + " \n " + job.getRequirements());
        body.add("custom_rules", customRules);

        // 1. LẤY NGƯỠNG LỌC ĐẦU VÀO TỪ ADMIN (SBERT Threshold)
        com.xuanthi.talentmatchingbe.entity.AiSetting settings = aiSettingService.getSettings();
        if (settings != null) {
            body.add("threshold", settings.getSbertRejectionThreshold());
        }

        // 2. LẤY TRỌNG SỐ CHẤM ĐIỂM TỪ JOB DO HR SET (LLM Weights)
        AiJobSetting jobSettings = job.getAiJobSetting();
        if (jobSettings == null) jobSettings = new AiJobSetting(); // Fallback an toàn

        body.add("w_exp", jobSettings.getWeightExp());
        body.add("w_skills", jobSettings.getWeightSkills());
        body.add("w_role", jobSettings.getWeightRole());
        body.add("w_tools", jobSettings.getWeightTools());
        body.add("w_edu", jobSettings.getWeightEdu());
        body.add("w_soft", jobSettings.getWeightSoft());

        return body;
    }

    private void processAiResponse(Application app, Map<String, Object> aiResult) throws Exception {
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
        Map<String, Object> analysisMap = new HashMap<>();
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
    }

    // ==============================================================
    // 3. QUẢN LÝ ĐƠN ỨNG TUYỂN VÀ XẾP HẠNG
    // ==============================================================
    public Page<ApplicationSimpleResponse> getApplicationsByJob(Long jobId, int page, int size) {
        User currentEmployer = SecurityUtils.getCurrentUser();
        if (currentEmployer == null || currentEmployer.getId() == null) {
            throw new RuntimeException("Unauthorized access!");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        if (job.getEmployer() == null || !job.getEmployer().getId().equals(currentEmployer.getId())) {
            throw new RuntimeException("You do not have access to this list!");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("matchScore").descending());
        return applicationRepository.findByJobIdPriority(jobId, pageable)
                .map(applicationMapper::toSimpleResponse);
    }

    // ✅ Bắt buộc phải có để đảm bảo tính toàn vẹn dữ liệu khi update
    @Transactional
    public void updateStatus(Long appId, ApplicationStatus status, String notes) {
        // Dùng hàm xịn này cho gọn, tự động check null
        User currentEmployer = SecurityUtils.getRequiredCurrentUser();

        Application app = applicationRepository.findByIdAndJobEmployerId(appId, currentEmployer.getId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không có quyền xử lý đơn ứng tuyển này!"));

        // 1. Cập nhật và lưu Database
        app.setStatus(status);
        if (notes != null && !notes.trim().isEmpty()) {
            app.setNotes(notes);
        }
        applicationRepository.save(app);

        // 2. 🚀 CHUẨN BỊ NỘI DUNG (Dùng chung cho cả Chuông và Email)
        String title;
        String jobTitle = app.getJob().getTitle();
        String messagePrefix = "Đơn ứng tuyển vị trí [" + jobTitle + "] của bạn ";
        String msg;
        String statusVi; // Khai báo biến ở đây để cả Email cũng dùng được

        switch (status) {
            case REVIEWING -> {
                title = "Hồ sơ đang được xem xét ⏳";
                msg = messagePrefix + "đang được Nhà tuyển dụng đánh giá.";
                statusVi = "Đang xem xét";
            }
            case INTERVIEW -> {
                title = "Mời phỏng vấn! 📅";
                msg = messagePrefix + "đã lọt vào vòng phỏng vấn. Vui lòng kiểm tra mục ghi chú hoặc email!";
                statusVi = "Mời phỏng vấn";
            }
            case ACCEPTED -> {
                title = "Chúc mừng trúng tuyển! 🎉";
                msg = messagePrefix + "đã được CHẤP NHẬN. Chào mừng bạn gia nhập đội ngũ!";
                statusVi = "Đã trúng tuyển";
            }
            case REJECTED -> {
                title = "Kết quả ứng tuyển 📝";
                msg = "Rất tiếc, " + messagePrefix + "chưa phù hợp với công ty tại thời điểm này. Chúc bạn may mắn lần sau!";
                statusVi = "Chưa phù hợp (Từ chối)";
            }
            default -> {
                title = "Cập nhật hồ sơ 🔄";
                msg = messagePrefix + "vừa được cập nhật trạng thái mới.";
                statusVi = "Cập nhật trạng thái";
            }
        }

        // 3. BÓP CÒ BẮN THÔNG BÁO REAL-TIME (Chuông Ting ting)
        notificationService.sendNotification(
                app.getCandidate().getId(),
                currentEmployer.getId(),
                title,
                msg,
                "APPLICATION_UPDATE",
                app.getId()
        );

        // 4. BÓP CÒ GỬI EMAIL (Bọc thêm check null cho tên công ty để an toàn 100%)
        String companyName = (app.getJob().getEmployer().getCompany() != null)
                ? app.getJob().getEmployer().getCompany().getName()
                : "Nhà tuyển dụng";

        mailService.sendApplicationStatusEmail(
                app.getCandidate().getEmail(),
                app.getCandidate().getFullName(),
                jobTitle,
                companyName,
                statusVi, // Đã hết lỗi Cannot resolve symbol nhé sếp!
                notes
        );
    }

    public Page<ApplicationResponse> getMyApplications(int page, int size) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new RuntimeException("Unauthorized access!");
        }

        Long userId = currentUser.getId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        return applicationRepository.findByCandidateIdOrderByAppliedAtDesc(userId, pageable)
                .map(applicationMapper::toResponse);
    }

    @Transactional
    public ApplicationResponse getApplicationDetail(Long appId) {
        // Dùng hàm tiện ích này cho xịn, đỡ phải viết if (currentEmployer == null)
        User currentEmployer = SecurityUtils.getRequiredCurrentUser();

        Long employerId = currentEmployer.getId();

        // Lấy đơn ứng tuyển, đồng thời chặn luôn nếu HR xem trộm đơn của công ty khác
        Application app = applicationRepository.findByIdAndJobEmployerId(appId, employerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn ứng tuyển hoặc bạn không có quyền xem!"));

        // ✅ LOGIC MỚI: DÙNG ENUM THAY CHO BOOLEAN
        // Nếu đơn đang ở trạng thái PENDING thì mới đổi thành VIEWED
        if (app.getStatus() == ApplicationStatus.PENDING) {
            app.setStatus(ApplicationStatus.VIEWED);
            applicationRepository.save(app);

            // 🚀 BÓP CÒ BẮN THÔNG BÁO CHO ỨNG VIÊN
            String companyName = (currentEmployer.getCompany() != null) ? currentEmployer.getCompany().getName() : "Một nhà tuyển dụng";

            notificationService.sendNotification(
                    app.getCandidate().getId(),   // Người nhận: Ứng viên
                    currentEmployer.getId(),      // Người gửi: HR
                    "Hồ sơ ứng tuyển đã được xem 👀",
                    companyName + " đã xem CV của bạn cho vị trí: " + app.getJob().getTitle() + ".",
                    "APPLICATION_VIEWED",         // Loại thông báo (Frontend dùng để đổi icon)
                    app.getId()                   // ID đơn ứng tuyển để ứng viên bấm vào xem lại
            );
        }

        return applicationMapper.toResponse(app);
    }

    // ==============================================================
    // 4. THỐNG KÊ DASHBOARD
    // ==============================================================
    public CandidateDashboardResponse getCandidateStats() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new RuntimeException("Unauthorized access!");
        }

        Long userId = currentUser.getId();
        return CandidateDashboardResponse.builder()
                .totalApplied(applicationRepository.countByCandidateId(userId))
                .pendingCount(applicationRepository.countByCandidateIdAndStatus(userId, ApplicationStatus.PENDING))
                .interviewCount(applicationRepository.countByCandidateIdAndStatus(userId, ApplicationStatus.INTERVIEW))
                .acceptedCount(applicationRepository.countByCandidateIdAndStatus(userId, ApplicationStatus.ACCEPTED))
                .rejectedCount(applicationRepository.countByCandidateIdAndStatus(userId, ApplicationStatus.REJECTED))
                .build();
    }

    public EmployerDashboardResponse getEmployerStats() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new RuntimeException("Unauthorized access!");
        }

        Long employerId = currentUser.getId();
        long activeJobs = jobRepository.countByEmployerIdAndStatus(employerId, JobStatus.OPEN);
        long totalApps = applicationRepository.countByJobEmployerId(employerId);
        long pendingApps = applicationRepository.countByJobEmployerIdAndStatus(employerId, ApplicationStatus.PENDING);
        long interviewApps = applicationRepository.countByJobEmployerIdAndStatus(employerId, ApplicationStatus.INTERVIEW);
        long unreadApps = applicationRepository.countByJobEmployerIdAndStatus(employerId, ApplicationStatus.PENDING);

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
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new RuntimeException("Unauthorized access!");
        }

        Long employerId = currentUser.getId();
        List<Object[]> results = applicationRepository.getMonthlyStatsNative(employerId);

        return results.stream()
                .map(result -> new MonthlyStatResponse(
                        (String) result[0],
                        ((Number) result[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    // ==========================================
    // ỨNG VIÊN RÚT ĐƠN ỨNG TUYỂN
    // ==========================================
    @Transactional // ✅ Bổ sung Transactional để đảm bảo toàn vẹn dữ liệu
    public String withdrawApplication(Long applicationId) {
        User currentCandidate = SecurityUtils.getRequiredCurrentUser();

        // 1. Tìm đơn ứng tuyển
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn ứng tuyển này!"));

        // 2. BẢO MẬT: Kiểm tra xem đơn này có đúng là của ông đang login không?
        if (!application.getCandidate().getId().equals(currentCandidate.getId())) {
            throw new AccessDeniedException("Bạn không có quyền rút đơn ứng tuyển của người khác!");
        }

        // 3. LOGIC CỐT LÕI: Chỉ cho rút khi HR chưa xem (Trạng thái đang là PENDING)
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Không thể rút đơn! Nhà tuyển dụng đã xem hoặc đang xử lý hồ sơ của bạn.");
        }

        // 4. "Quay xe" thành công
        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);

        // 5. 🚀 BÓP CÒ BẮN THÔNG BÁO CHO NHÀ TUYỂN DỤNG
        // Lấy tên ứng viên và tên Job cẩn thận để chống NullPointerException
        String candidateName = (currentCandidate.getFullName() != null) ? currentCandidate.getFullName() : "Một ứng viên";
        String jobTitle = (application.getJob() != null) ? application.getJob().getTitle() : "vị trí đang tuyển";

        notificationService.sendNotification(
                application.getJob().getEmployer().getId(), // Gửi cho: HR (Chủ của cái Job này)
                currentCandidate.getId(),                   // Người gửi: Ứng viên đang login
                "Ứng viên rút hồ sơ 🔙",
                candidateName + " vừa rút đơn ứng tuyển khỏi vị trí [" + jobTitle + "].",
                "APPLICATION_WITHDRAWN",                    // Phân loại
                application.getId()                         // Link tới đơn để HR kiểm tra
        );

        return "Đã rút đơn ứng tuyển thành công!";
    }
}
