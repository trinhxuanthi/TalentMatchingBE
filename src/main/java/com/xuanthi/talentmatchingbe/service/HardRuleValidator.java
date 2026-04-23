package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.application.CandidateApplyRequest;
import com.xuanthi.talentmatchingbe.entity.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HardRuleValidator {

    /**
     * Tính điểm trình độ học vấn
     *
     * @param eduLevel mức độ học vấn
     * @return điểm từ 0-5
     */
    private int getEducationScore(String eduLevel) {
        if (eduLevel == null) {
            return 0;
        }

        return switch (eduLevel.toUpperCase()) {
            case "HIGH_SCHOOL" -> 1;
            case "ASSOCIATE" -> 2;
            case "BACHELOR" -> 3;
            case "MASTER" -> 4;
            case "PHD" -> 5;
            case "NOT_REQUIRED" -> 0;
            default -> {
                log.warn("Unknown education level: {}", eduLevel);
                yield 0;
            }
        };
    }

    /**
     * Kiểm tra các quy tắc cứng (Hard Rules) cho ứng viên
     * Nếu vi phạm bất kỳ quy tắc nào, ứng viên sẽ bị từ chối tự động
     *
     * @param candidate dữ liệu ứng viên
     * @param job      thông tin công việc
     * @return kết quả validation
     */
    public ValidationResult checkHardRules(CandidateApplyRequest candidate, Job job) {
        if (candidate == null) {
            log.error("Candidate request is null");
            return ValidationResult.fail("Dữ liệu ứng viên không hợp lệ!");
        }

        if (job == null) {
            log.error("Job is null");
            return ValidationResult.fail("Thông tin công việc không hợp lệ!");
        }

        log.debug("Validating candidate application for job: {}", job.getTitle());

        // 1. Kiểm tra kinh nghiệm làm việc
        if (job.getMinExpYears() != null && job.getMinExpYears() > 0) {
            if (candidate.getYearsOfExperience() == null || candidate.getYearsOfExperience() < job.getMinExpYears()) {
                String reason = "Kinh nghiệm chưa đạt tối thiểu " + job.getMinExpYears() + " năm. (Hiện tại: " +
                        (candidate.getYearsOfExperience() != null ? candidate.getYearsOfExperience() : 0) + " năm)";
                log.info("Candidate failed experience check: {}", reason);
                return ValidationResult.fail(reason);
            }
        }

        // 2. Kiểm tra cấp bậc công việc
        if (candidate.getJobLevel() != null && job.getJobLevel() != null) {
            if (candidate.getJobLevel().toScore() < job.getJobLevel().toScore()) {
                String reason = "Cấp bậc hiện tại (" + candidate.getJobLevel() + ") thấp hơn yêu cầu (" + job.getJobLevel() + ").";
                log.info("Candidate failed job level check: {}", reason);
                return ValidationResult.fail(reason);
            }
        }

        // 3. Kiểm tra trình độ học vấn
        if (candidate.getEducationLevel() != null && job.getEducationLevel() != null) {
            int candidateEduScore = getEducationScore(candidate.getEducationLevel().name());
            int jobEduScore = getEducationScore(job.getEducationLevel().name());

            if (candidateEduScore < jobEduScore) {
                String reason = "Trình độ học vấn chưa đạt yêu cầu tối thiểu: " + job.getEducationLevel() +
                        ". (Hiện tại: " + candidate.getEducationLevel() + ")";
                log.info("Candidate failed education level check: {}", reason);
                return ValidationResult.fail(reason);
            }
        }

        // 4. Kiểm tra kỹ năng bắt buộc (phải MATCH 100%)
        return validateRequiredSkills(candidate, job);
    }

    /**
     * Kiểm tra kỹ năng bắt buộc
     *
     * @param candidate dữ liệu ứng viên
     * @param job      thông tin công việc
     * @return kết quả validation
     */
    private ValidationResult validateRequiredSkills(CandidateApplyRequest candidate, Job job) {
        List<String> requiredSkills = job.getRequiredSkills();
        List<String> candidateSkills = candidate.getCoreSkills();

        // Nếu job không yêu cầu kỹ năng cụ thể, pass
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            log.debug("Job does not require specific skills");
            return ValidationResult.success();
        }

        // Nếu ứng viên không khai báo kỹ năng, fail
        if (candidateSkills == null || candidateSkills.isEmpty()) {
            String reason = "Hồ sơ thiếu toàn bộ kỹ năng chuyên môn bắt buộc.";
            log.info("Candidate has no skills: {}", reason);
            return ValidationResult.fail(reason);
        }

        // Tìm kỹ năng bắt buộc mà ứng viên KHÔNG có
        List<String> missingSkills = requiredSkills.stream()
                .filter(requiredSkill -> candidateSkills.stream()
                        .noneMatch(candidateSkill -> candidateSkill.trim().equalsIgnoreCase(requiredSkill.trim())))
                .collect(Collectors.toList());

        // Nếu thiếu bất kỳ kỹ năng nào, fail (strict matching)
        if (!missingSkills.isEmpty()) {
            String reason = "Bạn thiếu kỹ năng bắt buộc: " + String.join(", ", missingSkills);
            log.info("Candidate missing skills: {}", reason);
            return ValidationResult.fail(reason);
        }

        log.debug("Candidate passed all hard rule validations");
        return ValidationResult.success();
    }

    /**
     * Validation result record
     *
     * @param isValid   true nếu pass tất cả quy tắc
     * @param failReason lý do từ chối (null nếu pass)
     */
    public record ValidationResult(boolean isValid, String failReason) {
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String reason) {
            return new ValidationResult(false, reason);
        }
    }
}