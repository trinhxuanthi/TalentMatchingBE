package com.xuanthi.talentmatchingbe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanthi.talentmatchingbe.entity.CandidateRanking;
import com.xuanthi.talentmatchingbe.entity.MatchingSession;
import com.xuanthi.talentmatchingbe.repository.CandidateRankingRepository;
import com.xuanthi.talentmatchingbe.repository.JobRepository;
import com.xuanthi.talentmatchingbe.repository.MatchingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuickMatchAiService {

    private final WebClient webClient = WebClient.create("http://localhost:5000");
    private final MatchingSessionRepository sessionRepository;
    private final CandidateRankingRepository rankingRepository;
    private final ObjectMapper objectMapper;
    private final JobRepository jobRepository;

    @Transactional
    // Sửa lại tham số đầu vào: thêm Long jobId
    public Object processAiMatching(List<String> cvUrls, Long jobId, String jdUrl, String jdText, String customRules) {
        List<String> validUrls = new ArrayList<>();
        List<Map<String, Object>> rejectedResults = new ArrayList<>();

        for (String url : cvUrls) {
            if (url == null || url.trim().isEmpty()) {
                rejectedResults.add(createRejectedResponse("Khong xac dinh", "Duong dan URL bi rong."));
                continue;
            }
            validUrls.add(url);
        }

        log.info("Java loc xong: {} URL hop le, {} bi loai.", validUrls.size(), rejectedResults.size());

        if (validUrls.isEmpty()) {
            return Map.of("status", "success", "leaderboard", List.of(), "failed_files", rejectedResults);
        }

        // ==========================================
        // LOGIC MỚI: TỰ ĐỘNG LẤY JD TỪ JOB ID
        // ==========================================
        String finalJdText = jdText != null ? jdText : "";
        String finalCustomRules = customRules != null ? customRules : "";
        String jobTitleForLog = "Nhap bang Text";

        if (jobId != null) {
            // Tìm Job trong DB
            var jobOptional = jobRepository.findById(jobId);
            if (jobOptional.isPresent()) {
                var job = jobOptional.get();
                jobTitleForLog = job.getTitle();
                // Cộng dồn Description và Requirements của Job vào jdText
                finalJdText = job.getTitle() + "\n" + job.getDescription() + "\n" + job.getRequirements() + "\n" + finalJdText;

                // Lấy kỹ năng bắt buộc nhét vào customRules để AI chấm chuẩn hơn
                if (job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty()) {
                    finalCustomRules = "Kỹ năng bắt buộc: " + String.join(", ", job.getRequiredSkills()) + "\n" + finalCustomRules;
                }
            }
        }

        // ==========================================
        // DONG GOI DATA BAN SANG PYTHON
        // ==========================================
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("jd_text", finalJdText.trim());
        bodyBuilder.part("custom_rules", finalCustomRules.trim());

        if (jdUrl != null && !jdUrl.trim().isEmpty()) {
            bodyBuilder.part("jd_url", jdUrl.trim());
        }

        for (String url : validUrls) {
            bodyBuilder.part("cv_urls", url.trim());
        }

        log.info("Dang day request sang AI Core Python cho Job: {}...", jobTitleForLog);

        Map pythonResponse = webClient.post()
                .uri("/api/quick-match")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // ==========================================
        // LUU DATABASE AN TOAN CHONG LOI NULL
        // ==========================================
        if (pythonResponse != null && "success".equals(pythonResponse.get("status"))) {
            List<Map<String, Object>> leaderboard = (List<Map<String, Object>>) pythonResponse.get("leaderboard");
            String currentSessionId = UUID.randomUUID().toString();

            MatchingSession session = new MatchingSession();
            session.setSessionId(currentSessionId);

            // Lưu tên file hoặc Tên Job vào DB cho dễ quản lý
            if (jobId != null) {
                session.setJdFileName("Job: " + jobTitleForLog);
            } else {
                session.setJdFileName(jdUrl != null && !jdUrl.isEmpty() ? jdUrl.substring(jdUrl.lastIndexOf("/") + 1) : "Nhap bang Text");
            }

            session.setJdText(finalJdText);
            session.setCustomRules(finalCustomRules);
            session.setTotalCvs(leaderboard.size() + rejectedResults.size());
            sessionRepository.save(session);

            List<CandidateRanking> entitiesToSave = new ArrayList<>();

            for (Map<String, Object> aiResult : leaderboard) {
                try {
                    CandidateRanking ranking = new CandidateRanking();
                    ranking.setSessionId(currentSessionId);
                    ranking.setCandidateFile((String) aiResult.get("candidate_file"));

                    if (aiResult.get("match_score") != null) {
                        ranking.setMatchScore(Integer.valueOf(aiResult.get("match_score").toString()));
                    }

                    // Tự động gán Recommendation dựa trên điểm nếu Python không trả về
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
                } catch (Exception e) {
                    log.error("Loi Parse Data luu DB tai CV [{}]: {}", aiResult.get("candidate_file"), e.getMessage());
                }
            }

            rankingRepository.saveAll(entitiesToSave);
            log.info("Da luu DB thanh cong Session: {} voi {} CV.", currentSessionId, entitiesToSave.size());

            pythonResponse.put("session_id", currentSessionId);
        }

        if (pythonResponse != null && pythonResponse.containsKey("failed_files")) {
            List failedFiles = (List) pythonResponse.get("failed_files");
            failedFiles.addAll(rejectedResults);
        }

        return pythonResponse;
    }

    private Map<String, Object> createRejectedResponse(String filename, String reason) {
        Map<String, Object> response = new HashMap<>();
        response.put("candidate_file", filename != null ? filename : "Unknown");
        response.put("error", "Bi loai boi Java (Vong gui xe): " + reason);
        return response;
    }
}