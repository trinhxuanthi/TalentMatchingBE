package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.job.JobResponse;
import com.xuanthi.talentmatchingbe.service.SavedJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Map;

/**
 * REST Controller cho các API liên quan đến việc lưu công việc của ứng viên
 */
@RestController
@RequestMapping("/api/candidate/saved-jobs")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Candidate SavedJobController", description = "APIs for candidate saved jobs management")
public class CandidateSavedJobController {

    private final SavedJobService savedJobService;

    /**
     * Toggle save/unsave job status (Nút trái tim)
     */
    @Operation(summary = "Toggle save/unsave job (Heart button)")
    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping("/{jobId}/toggle")
    public ResponseEntity<Map<String, String>> toggleSaveJob(
            @Parameter(description = "Job ID to toggle save status", required = true)
            @PathVariable Long jobId) {

        log.debug("Toggle save job request for jobId: {}", jobId);
        String message = savedJobService.toggleSaveJob(jobId);

        // ✅ Dùng Map.of cho ngắn gọn, xóa sạch try-catch
        return ResponseEntity.ok(Map.of("message", message));
    }

    /**
     * Lấy danh sách công việc đã lưu (có phân trang)
     */
    @Operation(summary = "Get saved jobs list for current candidate")
    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping
    public ResponseEntity<Page<JobResponse>> getMySavedJobs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        log.debug("Get saved jobs request - page: {}, size: {}", page, size);
        Page<JobResponse> result = savedJobService.getMySavedJobs(page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * Kiểm tra trạng thái đã lưu chưa để hiển thị icon trái tim trên Frontend
     */
    @Operation(summary = "Check if job is saved by current candidate")
    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/{jobId}/check")
    public ResponseEntity<Map<String, Boolean>> checkIsSaved(
            @Parameter(description = "Job ID to check save status", required = true)
            @PathVariable Long jobId) {

        log.debug("Check saved status request for jobId: {}", jobId);
        boolean isSaved = savedJobService.checkIsSaved(jobId);

        return ResponseEntity.ok(Map.of("isSaved", isSaved));
    }
}