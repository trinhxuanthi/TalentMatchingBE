package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.application.CandidateApplyRequest;
import com.xuanthi.talentmatchingbe.entity.Job;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HardRuleValidator {

    private int getEducationScore(String eduLevel) {
        if (eduLevel == null) return 0;
        return switch (eduLevel.toUpperCase()) {
            case "HIGH_SCHOOL" -> 1;
            case "ASSOCIATE" -> 2;
            case "BACHELOR" -> 3;
            case "MASTER" -> 4;
            case "PHD" -> 5;
            case "NOT_REQUIRED" -> 0;
            default -> 0;
        };
    }

    public ValidationResult checkHardRules(CandidateApplyRequest candidate, Job job) {

        // 1. Check Kinh nghiệm
        if (job.getMinExpYears() != null && job.getMinExpYears() > 0) {
            if (candidate.getYearsOfExperience() == null || candidate.getYearsOfExperience() < job.getMinExpYears()) {
                return ValidationResult.fail("Kinh nghiệm chưa đạt tối thiểu " + job.getMinExpYears() + " năm.");
            }
        }

        // 2. Check Cấp bậc (Job Level)
        if (candidate.getJobLevel() != null && job.getJobLevel() != null) {
            if (candidate.getJobLevel().toScore() < job.getJobLevel().toScore()) {
                return ValidationResult.fail("Cấp bậc hiện tại (" + candidate.getJobLevel() + ") thấp hơn yêu cầu (" + job.getJobLevel() + ").");
            }
        }

        // 3. Check Bằng cấp
        if (candidate.getEducationLevel() != null && job.getEducationLevel() != null) {
            if (getEducationScore(candidate.getEducationLevel().name()) < getEducationScore(job.getEducationLevel().name())) {
                return ValidationResult.fail("Trình độ học vấn chưa đạt yêu cầu tối thiểu: " + job.getEducationLevel());
            }
        }

        // ============================================================
        // LƯỠI DAO 4: KỸ NĂNG BẮT BUỘC (PHẢI MATCH 100%)
        // ============================================================
        List<String> requiredSkills = job.getRequiredSkills();
        List<String> candidateSkills = candidate.getCoreSkills();

        if (requiredSkills != null && !requiredSkills.isEmpty()) {
            if (candidateSkills == null || candidateSkills.isEmpty()) {
                return ValidationResult.fail("Hồ sơ thiếu toàn bộ kỹ năng chuyên môn bắt buộc.");
            }

            // Tìm những kỹ năng mà Job yêu cầu nhưng ứng viên KHÔNG có
            List<String> missingSkills = requiredSkills.stream()
                    .filter(req -> candidateSkills.stream()
                            .noneMatch(cand -> cand.trim().equalsIgnoreCase(req.trim())))
                    .toList();

            // 🔥 CHẾ ĐỘ THIẾU 1 CÁI LÀ CHÉM:
            if (!missingSkills.isEmpty()) {
                return ValidationResult.fail("Bạn thiếu kỹ năng bắt buộc: " + String.join(", ", missingSkills));
            }
        }

        return ValidationResult.success();
    }

    public record ValidationResult(boolean isValid, String failReason) {
        public static ValidationResult success() { return new ValidationResult(true, null); }
        public static ValidationResult fail(String reason) { return new ValidationResult(false, reason); }
    }
}