package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.job.JobResponse;
import com.xuanthi.talentmatchingbe.service.SavedJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/candidate/saved-jobs")
@RequiredArgsConstructor
@Tag(name = "CandidateSavedJobController", description = "Các API liên quan đến ứng viên lưu job")
public class CandidateSavedJobController {

    private final SavedJobService savedJobService;

    // 1. Bật/Tắt Lưu Job (Bấm nút trái tim)
    @Operation(summary = "Bật/Tắt Lưu Job (Bấm nút trái tim)")
    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping("/{jobId}/toggle")
    public ResponseEntity<Map<String, String>> toggleSaveJob(@PathVariable Long jobId) {
        String message = savedJobService.toggleSaveJob(jobId);

        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    // 2. Lấy danh sách Job đã lưu vào trang "Việc làm của tôi"
    @Operation(summary = "Lấy danh sách Job đã lưu vào trang việc làm của tôi")
    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping
    public ResponseEntity<Page<JobResponse>> getMySavedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(savedJobService.getMySavedJobs(page, size));
    }

    // 3. Kiểm tra trạng thái (Trả về true/false để Frontend tô đỏ/trắng icon)
    @Operation(summary = "Kiểm tra trạng thái (Trả về true/false để Frontend tô đỏ/trắng icon)")
    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/{jobId}/check")
    public ResponseEntity<Map<String, Boolean>> checkIsSaved(@PathVariable Long jobId) {
        boolean isSaved = savedJobService.checkIsSaved(jobId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isSaved", isSaved);
        return ResponseEntity.ok(response);
    }
}