package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.application.*;
import com.xuanthi.talentmatchingbe.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Application Controller", description = "Các API liên quan đến Apply CV và Quản lý Ứng viên")
public class ApplicationController {

    private final ApplicationService applicationService;

    // ==========================================
    // 1. LUỒNG ỨNG VIÊN (CANDIDATE)
    // ==========================================

    @PostMapping("/apply")
    @Operation(summary = "Ứng viên nộp đơn ứng tuyển",
            description = "Luồng này sẽ chạy qua 'Máy chém' Java (Match 100% Skill) trước khi đẩy sang AI Python.")
    public ResponseEntity<ApplicationResponse> applyForJob(@Valid @RequestBody CandidateApplyRequest request) {
        // ✅ Đã xóa try-catch: Để GlobalExceptionHandler xử lý lỗi nộp trùng/lọc cứng
        ApplicationResponse response = applicationService.applyForJob(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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
    @Operation(summary = "Nhà tuyển dụng xem danh sách hồ sơ của 1 JOB (Sắp xếp theo điểm AI)")
    public ResponseEntity<Page<ApplicationSimpleResponse>> getApplicationsByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(applicationService.getApplicationsByJob(jobId, page, size));
    }

    @PatchMapping("/{appId}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "HR Cập nhật trạng thái và thêm ghi chú (Chuẩn Request Body)")
    public ResponseEntity<Map<String, String>> updateApplicationStatus(
            @PathVariable Long appId,
            @Valid @RequestBody UpdateStatusRequest request) { // ✅ Nhận 1 cục JSON thay vì Query Param

        // Gọi Service xử lý
        applicationService.updateStatus(appId, request.getStatus(), request.getNotes());

        return ResponseEntity.ok(Map.of("message", "Đã cập nhật trạng thái hồ sơ thành công!"));
    }

    @GetMapping("/{appId}/detail")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Xem chi tiết 1 đơn ứng tuyển (Bao gồm phân tích AI)")
    public ResponseEntity<ApplicationResponse> getDetail(@PathVariable Long appId) {
        // ✅ Đã xóa try-catch
        return ResponseEntity.ok(applicationService.getApplicationDetail(appId));
    }

    // ==========================================
    // 3. DASHBOARD NHÀ TUYỂN DỤNG
    // ==========================================

    @GetMapping("/employer/stats")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Số liệu Dashboard tổng quan của Employer")
    public ResponseEntity<EmployerDashboardResponse> getEmployerStats() {
        // ✅ Đã xóa try-catch
        return ResponseEntity.ok(applicationService.getEmployerStats());
    }

    @GetMapping("/employer/monthly-stats")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Số liệu biểu đồ CV đã nộp theo tháng của Employer")
    public ResponseEntity<List<MonthlyStatResponse>> getMonthlyStats() {
        // ✅ Đã xóa try-catch
        return ResponseEntity.ok(applicationService.getMonthlyStats());
    }

    @PatchMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Ứng viên rút đơn ứng tuyển (Chỉ được phép khi HR chưa xem)")
    public ResponseEntity<Map<String, String>> withdrawApplication(@PathVariable Long id) {

        String message = applicationService.withdrawApplication(id);

        return ResponseEntity.ok(Map.of("message", message));
    }
}