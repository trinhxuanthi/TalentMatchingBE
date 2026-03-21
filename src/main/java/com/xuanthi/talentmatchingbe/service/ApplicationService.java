package com.xuanthi.talentmatchingbe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanthi.talentmatchingbe.dto.ai.AiAnalysisDTO;
import com.xuanthi.talentmatchingbe.dto.ai.QuickMatchResponse;
import com.xuanthi.talentmatchingbe.dto.ai.RankingResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    public String applyJob(ApplicationRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        // 1. Kiểm tra chống spam (Đã nộp rồi thì không cho nộp lại)
        if (applicationRepository.existsByJobIdAndCandidateIdAndIsActiveTrue(request.getJobId(), currentUser.getId())) {
            throw new RuntimeException("Bạn đã nộp đơn cho công việc này rồi!");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Công việc không tồn tại!"));

        // ==========================================
        // 🛑 BỘ LỌC CỨNG (VÒNG GỬI XE)
        // ==========================================
        boolean isRejected = false;
        String rejectReason = "";

        // Kiểm tra Hạn chót
        if (job.getDeadline() != null && job.getDeadline().isBefore(LocalDateTime.now())) {
            isRejected = true;
            rejectReason = "Công việc đã hết hạn nộp hồ sơ.";
        }

        // Kiểm tra Kinh nghiệm (Lấy số từ chuỗi, vd: "3-5 năm" -> 3)
        if (!isRejected) {
            int requiredYears = parseExperience(job.getExperienceLevel());
            if (request.getYearsOfExperience() < requiredYears) {
                isRejected = true;
                rejectReason = "Không đủ số năm kinh nghiệm (Yêu cầu tối thiểu: " + requiredYears + " năm).";
            }
        }

        // Kiểm tra Học vấn
        if (!isRejected) {
            if (getEducationRank(request.getEducationLevel()) < getEducationRank(job.getEducationLevel())) {
                isRejected = true;
                rejectReason = "Trình độ học vấn không đạt yêu cầu (Yêu cầu: " + job.getEducationLevel() + ").";
            }
        }

        // 2. Tạo Entity lưu vào Database
        Application app = Application.builder()
                .job(job)
                .candidate(currentUser)
                .cvUrl(request.getCvUrl())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .educationLevel(request.getEducationLevel())
                .yearsOfExperience(request.getYearsOfExperience())
                .status(isRejected ? ApplicationStatus.REJECTED : ApplicationStatus.PENDING)
                .notes(isRejected ? "Hệ thống tự động loại: " + rejectReason : null)
                .isAiScored(isRejected ? -2 : 0) // -2: Bị loại từ vòng gửi xe, 0: Chờ AI
                .appliedAt(LocalDateTime.now())
                .build();

        Application savedApp = applicationRepository.save(app);

        // 3. Xử lý sau khi lưu
        if (isRejected) {
            // Thông báo ngay cho ứng viên nếu bị loại
            notificationService.sendNotification(
                    currentUser.getId(),
                    job.getEmployer().getId(),
                    "Kết quả sơ loại hồ sơ",
                    "Rất tiếc, hồ sơ của bạn chưa đáp ứng yêu cầu tối thiểu cho vị trí: " + job.getTitle() + ". Lý do: " + rejectReason,
                    "APPLICATION",
                    savedApp.getId()
            );
            return "Hồ sơ của bạn đã được ghi nhận";
        }

        // Nếu qua vòng gửi xe, gọi AI phân tích ngầm
        callPythonAiToEvaluate(savedApp.getId(), job.getId());

        return "Nộp đơn thành công! ";
    }

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
    // 4. LẤY BẢNG XẾP HẠNG (RANKING) CHO EMPLOYER
    // ==============================================================
    public List<RankingResponse> getRankingByJob(Long jobId) {
        List<Application> apps = applicationRepository.findRankingByJobId(jobId);
        return apps.stream()
                .map(this::convertToRankingResponse)
                .collect(Collectors.toList());
    }

    private RankingResponse convertToRankingResponse(Application app) {
        AiAnalysisDTO analysisObj;

        if (app.getIsAiScored() != null && app.getIsAiScored() == 2 && app.getAiAnalysis() != null) {
            try {
                analysisObj = objectMapper.readValue(app.getAiAnalysis(), AiAnalysisDTO.class);
            } catch (JsonProcessingException e) {
                log.error("❌ Lỗi Parse JSON AI Analysis App ID {}: {}", app.getId(), e.getMessage());
                analysisObj = createDefaultAnalysis("Lỗi định dạng dữ liệu AI.");
            }
        } else {
            String msg = switch (app.getIsAiScored() == null ? 0 : app.getIsAiScored()) {
                case 0, 1 -> "Hệ thống đang phân tích...";
                case -1 -> "AI gặp lỗi khi đọc file CV này.";
                case -2 -> "Bị loại do không đáp ứng yêu cầu tối thiểu (Kinh nghiệm/Học vấn).";
                default -> "Chưa có dữ liệu.";
            };
            analysisObj = createDefaultAnalysis(msg);
        }

        return RankingResponse.builder()
                .applicationId(app.getId())
                .candidateName(app.getFullName() != null ? app.getFullName() : app.getCandidate().getFullName())
                .cvUrl(app.getCvUrl())
                .matchScore(app.getMatchScore() != null ? app.getMatchScore() : 0.0)
                .aiAnalysis(analysisObj)
                .appliedAt(app.getAppliedAt())
                .build();
    }

    private AiAnalysisDTO createDefaultAnalysis(String message) {
        AiAnalysisDTO dto = new AiAnalysisDTO();
        dto.setConclusion(message);

        AiAnalysisDTO.SkillsAnalysis skills = new AiAnalysisDTO.SkillsAnalysis();
        skills.setMatchedMustHave(List.of());
        skills.setMissingMustHave(List.of());
        skills.setMatchedNiceToHave(List.of());
        skills.setMissingNiceToHave(List.of());
        dto.setSkillsAnalysis(skills);

        return dto;
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

    // ==============================================================
    // 6. HELPER FUNCTIONS (So sánh logic)
    // ==============================================================

    /**
     * Quy đổi học vấn ra điểm (Rank) để so sánh
     */
    private int getEducationRank(String level) {
        if (level == null || level.trim().isEmpty()) return 0;
        return switch (level.trim().toLowerCase()) {
            case "tiến sĩ", "trên đại học" -> 6;
            case "thạc sĩ" -> 5;
            case "đại học", "cử nhân" -> 4;
            case "cao đẳng" -> 3;
            case "trung cấp" -> 2;
            case "thpt", "cấp 3" -> 1;
            default -> 0; // Lao động phổ thông
        };
    }

    /**
     * Tách số năm kinh nghiệm từ chuỗi (Vd: "Trên 3 năm" -> 3, "Không yêu cầu" -> 0)
     */
    private int parseExperience(String expText) {
        if (expText == null || expText.trim().isEmpty()) return 0;

        // Dùng Regex tìm cụm số đầu tiên trong chuỗi
        Matcher matcher = Pattern.compile("\\d+").matcher(expText);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 0; // Không có số nào -> 0 năm
    }

    // ==============================================================
    // 7. LUỒNG CHECK CV NÓNG (Dành cho Nhà Tuyển Dụng)
    // ==============================================================
    public QuickMatchResponse quickCheckCv(Long jobId, String cvUrl) {
        // 1. Lấy thông tin Job để làm gốc so sánh
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Công việc không tồn tại!"));

        try {
            // 2. Lấy từ điển động
            Map<String, String> dynamicAliases = skillAliasService.getDynamicAliasesMap();

            // 3. Chuẩn bị Payload gửi Python
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("cv_url", cvUrl);
            requestPayload.put("job_requirements", job.getDescription() + " " + job.getRequirements());
            requestPayload.put("job_skills_json", job.getRequiredSkills());
            requestPayload.put("dynamic_aliases", dynamicAliases);

            log.info("🔥 [QUICK CHECK] Đang chấm nóng CV: {}", cvUrl);

            // 4. Gọi Python ĐỒNG BỘ (Code sẽ đứng chờ ở đây khoảng 2-3s cho đến khi AI trả kết quả)
            ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_AI_URL, requestPayload, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> aiResult = response.getBody();

                // Lấy điểm
                Double score = Double.valueOf(aiResult.get("match_score").toString());

                // Parse dữ liệu phân tích thành Object để gửi về Frontend
                String analysisJson = objectMapper.writeValueAsString(aiResult.get("ai_analysis"));
                AiAnalysisDTO analysisObj = objectMapper.readValue(analysisJson, AiAnalysisDTO.class);

                log.info("✅ [QUICK CHECK] Hoàn tất! Điểm: {}", score);

                // Trả thẳng kết quả về cho Frontend, KHÔNG lưu vào bảng Application
                return QuickMatchResponse.builder()
                        .matchScore(score)
                        .aiAnalysis(analysisObj)
                        .build();
            } else {
                throw new RuntimeException("AI Server trả về lỗi.");
            }
        } catch (Exception e) {
            log.error("❌ [QUICK CHECK] Lỗi chấm nóng: {}", e.getMessage());
            throw new RuntimeException("Không thể phân tích CV lúc này. Vui lòng thử lại sau.");
        }
    }
}