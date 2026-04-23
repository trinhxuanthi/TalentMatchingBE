package com.xuanthi.talentmatchingbe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanthi.talentmatchingbe.dto.job.AiWeightDto;
import com.xuanthi.talentmatchingbe.entity.*;
import com.xuanthi.talentmatchingbe.repository.CandidateRankingRepository;
import com.xuanthi.talentmatchingbe.repository.JobRepository;
import com.xuanthi.talentmatchingbe.repository.MatchingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Service xử lý AI Matching cho ứng viên
 * - Gửi CV và Job Description tới Python AI Core
 * - Lưu kết quả matching vào database
 * - Hỗ trợ tùy chỉnh quy tắc đánh giá
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QuickMatchAiService {

    private final MatchingSessionRepository sessionRepository;
    private final CandidateRankingRepository rankingRepository;
    private final ObjectMapper objectMapper;
    private final JobRepository jobRepository;
    private final AiSettingService aiSettingService;

    @Value("${python.ai.url:http://localhost:5000/api/quick-match}")
    private String pythonAiUrl;

    // Tạo WebClient bean thay vì inject
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
            .build();

    /**
     * Xử lý kết quả matching từ AI Python
     * 
     * @param cvUrls danh sách URL CV (bắt buộc, không rỗng)
     * @param jobId ID công việc trong hệ thống (tùy chọn)
     * @param jdUrl URL Job Description (tùy chọn)
     * @param jdText nội dung Job Description (tùy chọn)
     * @param customRules các quy tắc tùy chỉnh (tùy chọn)
     * @return Map chứa kết quả matching từ Python
     * @throws IllegalArgumentException nếu cvUrls null hoặc rỗng
     */

    /**
     * LUỒNG HR: Lọc CV chủ động (Sử dụng Gemini qua /api/quick-match)
     */
    @Transactional
    public Object processAiMatching(List<String> cvUrls, Long jobId, String jdUrl, String jdText, String customRules, AiWeightDto manualWeights) {
        if (cvUrls == null || cvUrls.isEmpty()) {
            throw new IllegalArgumentException("Danh sách CV URLs không được rỗng!");
        }

        // 1. Lấy ngưỡng lọc SBERT từ Admin (Màng lọc vòng ngoài)
        AiSetting adminSettings = aiSettingService.getSettings();
        Double threshold = adminSettings.getSbertRejectionThreshold();

        // 2. Trọng số LLM mặc định (Nếu không có JobId hoặc Job không có setting riêng)
        Double wExp = 0.3, wSkills = 0.3, wRole = 0.15, wTools = 0.1, wEdu = 0.1, wSoft = 0.05;

        String finalJdText = jdText != null ? jdText : "";
        String finalCustomRules = customRules != null ? customRules : "";
        String jobTitleForLog = "Manual Text";

        if (jobId != null) {
            Job job = jobRepository.findById(jobId).orElse(null);
            if (job != null) {
                jobTitleForLog = job.getTitle();
                finalJdText = job.getTitle() + "\n" + job.getDescription() + "\n" + job.getRequirements() + "\n" + finalJdText;

                // ✅ Ưu tiên lấy trọng số HR đã thiết lập riêng cho Job này
                AiJobSetting hrSettings = job.getAiJobSetting();
                if (hrSettings != null) {
                    wExp = hrSettings.getWeightExp();
                    wSkills = hrSettings.getWeightSkills();
                    wRole = hrSettings.getWeightRole();
                    wTools = hrSettings.getWeightTools();
                    wEdu = hrSettings.getWeightEdu();
                    wSoft = hrSettings.getWeightSoft();
                }
            }
        }

        // 3. ✅ ƯU TIÊN CAO NHẤT: Nếu HR truyền trực tiếp trọng số từ giao diện Check nóng, ghi đè tất cả
        if (manualWeights != null) {
            if (manualWeights.getWeightExp() != null) wExp = manualWeights.getWeightExp();
            if (manualWeights.getWeightSkills() != null) wSkills = manualWeights.getWeightSkills();
            if (manualWeights.getWeightRole() != null) wRole = manualWeights.getWeightRole();
            if (manualWeights.getWeightTools() != null) wTools = manualWeights.getWeightTools();
            if (manualWeights.getWeightEdu() != null) wEdu = manualWeights.getWeightEdu();
            if (manualWeights.getWeightSoft() != null) wSoft = manualWeights.getWeightSoft();
        }

        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("jd_text", finalJdText.trim());
            bodyBuilder.part("custom_rules", finalCustomRules.trim());

            // ✅ Bổ sung tham số cấu hình động gửi sang Python
            bodyBuilder.part("threshold", threshold);
            bodyBuilder.part("w_exp", wExp);
            bodyBuilder.part("w_skills", wSkills);
            bodyBuilder.part("w_role", wRole);
            bodyBuilder.part("w_tools", wTools);
            bodyBuilder.part("w_edu", wEdu);
            bodyBuilder.part("w_soft", wSoft);

            if (jdUrl != null && !jdUrl.trim().isEmpty()) bodyBuilder.part("jd_url", jdUrl.trim());
            for (String url : cvUrls) bodyBuilder.part("cv_urls", url.trim());

            Map pythonResponse = webClient.post()
                    .uri(pythonAiUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> Mono.empty())
                    .block();

            return processPythonResponse(pythonResponse, jobTitleForLog, new ArrayList<>(), finalJdText, finalCustomRules);
        } catch (Exception e) {
            log.error("Lỗi xử lý AI matching: {}", e.getMessage());
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    /**
     * Xử lý response từ Python AI và lưu vào database
     */
    private Object processPythonResponse(Map<String, Object> pythonResponse, String jobTitleForLog, 
                                         List<Map<String, Object>> rejectedResults, 
                                         String finalJdText, String finalCustomRules) {
        if (pythonResponse == null || !"success".equals(pythonResponse.get("status"))) {
            log.warn("Python AI response không thành công: {}", pythonResponse);
            return pythonResponse;
        }

        try {
            List<Map<String, Object>> leaderboard = (List<Map<String, Object>>) pythonResponse.get("leaderboard");
            if (leaderboard == null) {
                log.warn("Không tìm thấy leaderboard trong response");
                return pythonResponse;
            }

            String currentSessionId = UUID.randomUUID().toString();

            MatchingSession session = new MatchingSession();
            session.setSessionId(currentSessionId);

            // Lưu tên file hoặc Tên Job vào DB
            if (jobTitleForLog != null && !jobTitleForLog.isEmpty()) {
                session.setJdFileName("Job: " + jobTitleForLog);
            } else {
                session.setJdFileName("Nhập bằng Text");
            }

            session.setJdText(finalJdText);
            session.setCustomRules(finalCustomRules);
            session.setTotalCvs(leaderboard.size() + rejectedResults.size());
            sessionRepository.save(session);
            log.info("Lưu MatchingSession: {}", currentSessionId);

            List<CandidateRanking> entitiesToSave = new ArrayList<>();

            // Xử lý từng kết quả từ AI
            for (Map<String, Object> aiResult : leaderboard) {
                try {
                    CandidateRanking ranking = new CandidateRanking();
                    ranking.setSessionId(currentSessionId);
                    ranking.setCandidateFile((String) aiResult.get("candidate_file"));

                    if (aiResult.get("match_score") != null) {
                        ranking.setMatchScore(Integer.valueOf(aiResult.get("match_score").toString()));
                    }

                    // Tự động gán Recommendation dựa trên điểm
                    if (aiResult.get("recommendation") != null) {
                        ranking.setRecommendation((String) aiResult.get("recommendation"));
                    } else {
                        int score = ranking.getMatchScore() != null ? ranking.getMatchScore() : 0;
                        ranking.setRecommendation(score >= 75 ? "ĐỀ XUẤT" : (score >= 50 ? "XEM XÉT" : "TỪ CHỐI"));
                    }

                    Map<String, Integer> breakdown = (Map<String, Integer>) aiResult.get("score_breakdown");
                    if (breakdown != null) {
                        ranking.setExpScore(breakdown.get("experience"));
                        ranking.setSkillScore(breakdown.get("skills"));
                        ranking.setRoleScore(breakdown.get("role_alignment"));
                    }

                    // Xây dựng detailed analysis JSON
                    Map<String, Object> analysisMap = new HashMap<>();
                    analysisMap.put("jd_core_evaluation", aiResult.get("jd_core_evaluation"));
                    analysisMap.put("custom_rules_evaluation", aiResult.get("custom_rules_evaluation"));
                    analysisMap.put("executive_summary", aiResult.get("executive_summary"));

                    if (aiResult.get("ai_analysis") != null) {
                        analysisMap.put("ai_analysis", aiResult.get("ai_analysis"));
                    }

                    String detailedJson = objectMapper.writeValueAsString(analysisMap);
                    ranking.setDetailedAnalysisJson(detailedJson);

                    entitiesToSave.add(ranking);
                    log.debug("Thêm CandidateRanking: {}", ranking.getCandidateFile());
                } catch (Exception e) {
                    log.error("Lỗi parse data lưu DB tại CV [{}]: {}", aiResult.get("candidate_file"), e.getMessage());
                }
            }

            rankingRepository.saveAll(entitiesToSave);
            log.info("Đã lưu DB thành công Session: {} với {} CV.", currentSessionId, entitiesToSave.size());

            pythonResponse.put("session_id", currentSessionId);

            // Merge rejected results với failed_files từ Python
            if (pythonResponse.containsKey("failed_files")) {
                List<Map<String, Object>> failedFiles = (List<Map<String, Object>>) pythonResponse.get("failed_files");
                if (failedFiles != null) {
                    failedFiles.addAll(rejectedResults);
                }
            } else {
                pythonResponse.put("failed_files", rejectedResults);
            }

            return pythonResponse;
        } catch (Exception e) {
            log.error("Lỗi xử lý Python response: {}", e.getMessage(), e);
            return Map.of("status", "error", "message", "Lỗi xử lý response: " + e.getMessage());
        }
    }

    /**
     * Tạo response cho CV bị từ chối
     */
    private Map<String, Object> createRejectedResponse(String filename, String reason) {
        Map<String, Object> response = new HashMap<>();
        response.put("candidate_file", filename != null ? filename : "Unknown");
        response.put("error", "Bị loại bởi Java (Vòng lọc trước): " + reason);
        return response;
    }


    /**
     * [HYBRID] API 1: Bóc tách CV hoặc JD ra 3 thông tin cốt lõi (JSON)
     * Gọi hàm này khi Ứng viên Upload CV hoặc HR đăng Job.
     */
    public Map<String, Object> extractCoreEntities(String fileUrl) {
        // Tự động cắt bỏ /quick-match (nếu có) để nối đường dẫn mới, không đụng chạm đến config cũ
        String baseUrl = pythonAiUrl.endsWith("/quick-match")
                ? pythonAiUrl.replace("/quick-match", "")
                : pythonAiUrl;
        String url = baseUrl + "/ai/extract-core";

        log.info("Bắt đầu nhờ Gemini bóc tách file: {}", fileUrl);

        try {
            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("file_url", fileUrl))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnError(error -> log.error("Lỗi khi bóc tách Core Entity: {}", error.getMessage()))
                    .block();
        } catch (Exception e) {
            log.error("Ngoại lệ khi gọi Python Extract Core: {}", e.getMessage());
            return null;
        }
    }

    /**
     * [HYBRID] API 2: Smart Match siêu tốc dùng SBERT Vector (Chạy Offline 100%)
     * Gọi hàm này khi User vào tab "Gợi ý việc làm" hoặc chạy ngầm (Background Task).
     *
     * @param targetCv      Dữ liệu CV đã bóc tách (Map chứa: id, standard_title, skills, years_of_experience)
     * @param candidateJobs Danh sách các Job đã bóc tách (List các Map tương tự CV)
     * @return Map chứa kết quả điểm đã được Python sắp xếp từ cao xuống thấp
     */
    /**
     * LUỒNG ADMIN/HỆ THỐNG: Smart Match chạy ngầm (Sử dụng SBERT Vector qua /api/ai/smart-match)
     */
    public Map<String, Object> smartMatchJobs(Map<String, Object> targetCv, List<Map<String, Object>> candidateJobs, AiSetting settings) {
        String baseUrl = pythonAiUrl.replace("/quick-match", "");
        String url = baseUrl + "/ai/smart-match";

        // ✅ Truyền trọng số SBERT từ cấu hình Admin sang Python để tính toán Vector động
        Map<String, Object> requestBody = Map.of(
                "target", targetCv,
                "candidates", candidateJobs,
                "w_title", settings.getSbertWeightTitle(),
                "w_skills", settings.getSbertWeightSkills(),
                "w_exp", settings.getSbertWeightExp()
        );

        try {
            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.error("Ngoại lệ khi gọi Python Smart Match: {}", e.getMessage());
            return null;
        }
    }
}





