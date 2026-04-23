package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.ai.QuickMatchRequest;
import com.xuanthi.talentmatchingbe.dto.job.JobMatchResponse;
import com.xuanthi.talentmatchingbe.service.JobMatchService;
import com.xuanthi.talentmatchingbe.service.QuickMatchAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for AI Core operations
 * Handles AI-powered CV matching against job descriptions
 */
@RestController
@RequestMapping("/api/v1/ai-core")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "AI Core Controller", description = "APIs for direct interaction with Python AI services")
public class AiMatchController {

    private final JobMatchService jobMatchService;
    private final QuickMatchAiService quickMatchAiService;

    /**
     * Trigger AI matching for multiple CVs against job description
     */
    @PostMapping("/quick-match")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Quick match CVs against JD", description = "Scan multiple CVs (URLs) against JD (URL or Text)")
    public ResponseEntity<Object> triggerAiMatching(@Valid @RequestBody QuickMatchRequest request) {

        log.info("AI matching request received - CVs: {}, JobId: {}",
                request.getCvUrls() != null ? request.getCvUrls().size() : 0, request.getJobId());

        // ✅ Business rule 1: Kiểm tra danh sách CV
        if (request.getCvUrls() == null || request.getCvUrls().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp ít nhất 1 liên kết CV.");
        }

        boolean hasJdUrl = request.getJdUrl() != null && !request.getJdUrl().trim().isEmpty();
        boolean hasJdText = request.getJdText() != null && !request.getJdText().trim().isEmpty();
        boolean hasJobId = request.getJobId() != null;

        // ✅ Business rule 2: Kiểm tra nguồn JD
        if (!hasJdUrl && !hasJdText && !hasJobId) {
            throw new IllegalArgumentException("Vui lòng cung cấp JD (URL, Text) HOẶC chọn một Job ID hiện có.");
        }

        // ✅ Gọi Service - Nếu service lỗi (Timeout, 500...), GlobalExceptionHandler sẽ tự bắt
        Object result = quickMatchAiService.processAiMatching(
                request.getCvUrls(),
                request.getJobId(),
                request.getJdUrl(),
                request.getJdText(),
                request.getCustomRules(),
                request.getAiSettings()
        );

        log.info("AI matching completed successfully");
        return ResponseEntity.ok(result);
    }

    /**
     * API cho Ứng viên xem danh sách Job gợi ý
     * URL: GET /api/matches/candidates/{candidateId}/jobs
     */
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    @Operation(summary = "Xem danh sách Job gợi ý cho Ứng viên", description = "Ứng viên có thể xem danh sách Job phù hợp nhất với mình, sắp xếp theo điểm từ cao xuống thấp")
    @GetMapping("/candidates/{candidateId}/jobs")
    public ResponseEntity<List<JobMatchResponse.ForCandidate>> getRecommendedJobsForCandidate(
            @PathVariable Long candidateId) {

        List<JobMatchResponse.ForCandidate> response = jobMatchService.getRecommendedJobs(candidateId);
        return ResponseEntity.ok(response);
    }
}