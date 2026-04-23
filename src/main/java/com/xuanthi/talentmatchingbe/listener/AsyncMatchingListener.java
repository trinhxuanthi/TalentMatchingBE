package com.xuanthi.talentmatchingbe.listener;

import com.xuanthi.talentmatchingbe.entity.*;
import com.xuanthi.talentmatchingbe.event.CvUpdatedEvent;
import com.xuanthi.talentmatchingbe.repository.CandidateProfileRepository;
import com.xuanthi.talentmatchingbe.repository.JobMatchRepository;
import com.xuanthi.talentmatchingbe.repository.JobRepository;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.service.AiSettingService;
import com.xuanthi.talentmatchingbe.service.QuickMatchAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncMatchingListener {

    private final CandidateProfileRepository profileRepository;
    private final JobRepository jobRepository;
    private final JobMatchRepository jobMatchRepository;
    private final UserRepository userRepository; // Nhúng thêm thằng này để lấy User
    private final QuickMatchAiService quickMatchAiService;
    private final AiSettingService aiSettingService;

    @Async // Tách luồng chạy ngầm siêu tốc
    @EventListener
    @Transactional
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3, // Thử lại tối đa 3 lần
            backoff = @Backoff(delay = 2000, multiplier = 2) // Lần 1 đợi 2s, lần 2 đợi 4s, lần 3 đợi 8s
    )// Đảm bảo an toàn dữ liệu
    public void handleCvUpdated(CvUpdatedEvent event) {
        Long candidateId = event.getCandidateId();
        String cvUrl = event.getCvUrl();

        log.info("[AI WORKER] Bắt đầu tiến trình bóc tách và tính điểm ngầm cho Candidate ID: {}", candidateId);

        try {
            // ✅ BƯỚC 0: LẤY CẤU HÌNH TỪ ADMIN (RAM)
            // Không dùng số fix cứng 70.0 hay 50.0 nữa
            AiSetting settings = aiSettingService.getSettings();

            // ========================================================
            // BƯỚC 1: GỌI AI BÓC TÁCH CV -> LƯU VÀO PROFILE
            // ========================================================
            log.info("[AI WORKER] Đang gọi Gemini AI bóc tách CV từ link: {}", cvUrl);
            Map<String, Object> aiExtractedData = quickMatchAiService.extractCoreEntities(cvUrl);

            if (aiExtractedData == null || aiExtractedData.isEmpty()) {
                log.error("[AI WORKER] ❌ Thất bại! Không bóc tách được CV.");
                return;
            }

            User candidate = userRepository.findById(candidateId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy User ID: " + candidateId));

            CandidateProfile profile = profileRepository.findByUserId(candidateId)
                    .orElse(new CandidateProfile());

            profile.setUser(candidate);
            profile.setStandardTitle((String) aiExtractedData.get("standard_title"));
            profile.setSkills((String) aiExtractedData.get("skills"));

            Object expObj = aiExtractedData.get("years_of_experience");
            if (expObj != null) {
                profile.setYearsOfExperience(((Number) expObj).intValue());
            }

            profileRepository.save(profile);
            log.info("[AI WORKER] ✅ Đã bóc tách và lưu CandidateProfile thành công!");

            // ========================================================
            // BƯỚC 2: MANG PROFILE ĐI MATCHING VỚI CÁC JOB
            // ========================================================
            Map<String, Object> targetCv = Map.<String, Object>of(
                    "id", candidateId,
                    "standard_title", profile.getStandardTitle() != null ? profile.getStandardTitle() : "",
                    "skills", profile.getSkills() != null ? profile.getSkills() : "",
                    "years_of_experience", profile.getYearsOfExperience() != null ? profile.getYearsOfExperience() : 0
            );

            List<Job> activeJobs = jobRepository.findAll();
            if (activeJobs.isEmpty()) {
                log.info("[AI WORKER] Hệ thống chưa có Job nào để tính điểm.");
                return;
            }

            List<Map<String, Object>> candidateJobs = activeJobs.stream().map(job -> Map.<String, Object>of(
                    "id", job.getId(),
                    "standard_title", job.getTitle() != null ? job.getTitle() : "",
                    "skills", job.getRequiredSkills() != null ? String.join(", ", job.getRequiredSkills()) : "",
                    "years_of_experience", job.getMinExpYears() != null ? job.getMinExpYears() : 0
            )).collect(Collectors.toList());

            log.info("[AI WORKER] Đang chấm điểm SBERT Vector với {} Job theo trọng số Admin...", candidateJobs.size());

            // ✅ THAY ĐỔI: Truyền thêm 'settings' vào để Python lấy trọng số SBERT (w_title, w_skills, w_exp)
            Map<String, Object> matchResult = quickMatchAiService.smartMatchJobs(targetCv, candidateJobs, settings);

            if (matchResult == null || !matchResult.containsKey("matches")) {
                log.error("[AI WORKER] ❌ Python AI trả về kết quả lỗi hoặc rỗng!");
                return;
            }

            // ========================================================
            // BƯỚC 3: XỬ LÝ KẾT QUẢ VÀ LƯU BẢNG ĐIỂM (DÙNG MÀNG LỌC ADMIN)
            // ========================================================
            List<Map> matches = (List<Map>) matchResult.get("matches");
            for (Map match : matches) {
                Long jobId = ((Number) match.get("id")).longValue();
                Double score = ((Number) match.get("match_score")).doubleValue();

                Map<String, Object> breakdown = (Map<String, Object>) match.get("breakdown");
                Double skillScore = ((Number) breakdown.get("skills_score")).doubleValue();
                Double titleScore = ((Number) breakdown.get("title_score")).doubleValue();
                Double expScore = ((Number) breakdown.get("exp_score")).doubleValue();

                // 🚨 MÀNG LỌC 1: DÙNG BIẾN ADMIN filterTitleScore (thay vì 70.0)
                if (titleScore < settings.getFilterTitleScore()) {
                    log.info("[BỘ LỌC] Loại bỏ Job ID {} vì sai lệch chức danh (Score: {} < Ngưỡng: {})",
                            jobId, titleScore, settings.getFilterTitleScore());
                    continue;
                }

                // 🚨 MÀNG LỌC 2: DÙNG BIẾN ADMIN filterTotalScore (thay vì 50.0)
                if (score < settings.getFilterTotalScore()) {
                    log.info("[BỘ LỌC] Loại bỏ Job ID {} vì điểm tổng thấp (Score: {} < Ngưỡng: {})",
                            jobId, score, settings.getFilterTotalScore());
                    continue;
                }

                Job job = jobRepository.findById(jobId).orElse(null);
                if (job == null) continue;

                JobMatch jobMatch = jobMatchRepository.findByCandidateIdAndJobId(candidateId, jobId)
                        .orElse(JobMatch.builder()
                                .candidate(candidate)
                                .job(job)
                                .isNotified(false)
                                .build());

                jobMatch.setMatchScore(score);
                jobMatch.setSkillScore(skillScore);
                jobMatch.setTitleScore(titleScore);
                jobMatch.setExpScore(expScore);

                jobMatchRepository.save(jobMatch);
            }

            log.info("✅ XONG TOÀN BỘ! Đã cày xong bảng điểm cho ứng viên {}", candidateId);

        } catch (Exception e) {
            log.error("❌ Lỗi Hệ thống AI ngầm: {}", e.getMessage(), e);
        }
    }

    @Recover
    public void recover(Exception e, CvUpdatedEvent event) {
        log.error("🚨 CHỊU THUA! Sau 3 lần thử lại vẫn không thể tính điểm cho Candidate {}. Lỗi: {}",
                event.getCandidateId(), e.getMessage());
        // Ở đây sếp có thể gửi mail báo cho Admin hoặc đánh dấu trạng thái "FAILED" vào DB
    }
}