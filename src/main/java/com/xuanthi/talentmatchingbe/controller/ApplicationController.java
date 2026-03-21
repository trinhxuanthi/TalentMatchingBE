package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.ai.QuickMatchResponse;
import com.xuanthi.talentmatchingbe.dto.ai.RankingResponse;
import com.xuanthi.talentmatchingbe.dto.application.*;
import com.xuanthi.talentmatchingbe.enums.ApplicationStatus;
import com.xuanthi.talentmatchingbe.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "ApplicationController", description = "Các API liên quan đến Apply CV và Quản lý Ứng viên")
public class ApplicationController {

    private final ApplicationService applicationService;

    // ==========================================
    // 1. LUỒNG ỨNG VIÊN (CANDIDATE)
    // ==========================================

    @PostMapping("/apply")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Ứng viên nộp Form và Link CV")
    public ResponseEntity<String> apply(@Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.applyJob(request));
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Ứng viên xem lịch sử đã ứng tuyển")
    public ResponseEntity<Page<ApplicationResponse>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getMyApplications(page, size));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Số liệu Dashboard thống kê Job của Ứng viên")
    public ResponseEntity<CandidateDashboardResponse> getStats() {
        return ResponseEntity.ok(applicationService.getCandidateStats());
    }

    // ==========================================
    // 2. LUỒNG NHÀ TUYỂN DỤNG (EMPLOYER)
    // ==========================================

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Nhà tuyển dụng xem chi tiết toàn bộ hồ sơ của 1 JOB")
    public ResponseEntity<Page<ApplicationResponse>> getByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getApplicationsByJob(jobId, page, size));
    }

    @PatchMapping("/{appId}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Nhà tuyển dụng Cập nhật trạng thái (Duyệt/Loại/Phỏng vấn) và thêm Ghi chú")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long appId,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String notes) {
        applicationService.updateStatus(appId, status, notes);
        return ResponseEntity.ok("Cập nhật trạng thái ứng viên thành công!");
    }

    // 🔥 API QUAN TRỌNG NHẤT: XẾP HẠNG AI
    @GetMapping("/ranking/{jobId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')") // Đã khóa bảo mật
    @Operation(summary = "Nhà tuyển dụng xem Bảng xếp hạng CV theo điểm AI (Ranking List)")
    public ResponseEntity<List<RankingResponse>> getRankingByJob(@PathVariable Long jobId) {
        List<RankingResponse> rankingList = applicationService.getRankingByJob(jobId);
        return ResponseEntity.ok(rankingList);
    }

    // ==========================================
    // 3. DASHBOARD NHÀ TUYỂN DỤNG
    // ==========================================

    @GetMapping("/employer/stats")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Số liệu Dashboard tổng quan của Employer")
    public ResponseEntity<EmployerDashboardResponse> getEmployerStats() {
        return ResponseEntity.ok(applicationService.getEmployerStats());
    }

    @GetMapping("/employer/monthly-stats")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Số liệu biểu đồ CV đã nộp theo tháng của Employer")
    public ResponseEntity<List<MonthlyStatResponse>> getMonthlyStats() {
        return ResponseEntity.ok(applicationService.getMonthlyStats());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    @PostMapping("/jobs/{jobId}/quick-check")
    @Operation(summary = "Check CV AI nóng dành cho Employer")
    public ResponseEntity<QuickMatchResponse> quickCheckCandidateCv(
            @PathVariable Long jobId,
            @RequestParam String cvUrl) {

        QuickMatchResponse result = applicationService.quickCheckCv(jobId, cvUrl);
        return ResponseEntity.ok(result);
    }
}