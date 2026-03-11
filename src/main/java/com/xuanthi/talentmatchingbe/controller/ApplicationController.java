package com.xuanthi.talentmatchingbe.controller;

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
@Tag(name = "ApplicationController", description = "Các API liên quan đến Apply CV")
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * ỨNG VIÊN NỘP ĐƠN
     */
    @PostMapping("/apply")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Ứng viên nộp đơn")
    public ResponseEntity<String> apply(@Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.applyJob(request));
    }

    /**
     * NHÀ TUYỂN DỤNG XEM DANH SÁCH ỨNG VIÊN CỦA 1 JOB
     * Phân trang để load cực nhanh dù có hàng nghìn CV
     */
    @Operation(summary = "Nhà tuyển dụng xem ứng viên của 1 JOB")
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<Page<ApplicationResponse>> getByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getApplicationsByJob(jobId, page, size));
    }

    /**
     * NHÀ TUYỂN DỤNG DUYỆT/LOẠI CV (Cập nhật trạng thái)
     */
    @Operation(summary = "Nhà tuyển dụng duyệt loại CV và lí do loại")
    @PatchMapping("/{appId}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long appId,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String notes) {
        applicationService.updateStatus(appId, status, notes);
        return ResponseEntity.ok("Cập nhật trạng thái thành công!");
    }

    /**
     * ỨNG VIÊN XEM LỊCH SỬ ỨNG TUYỂN
     */
    @Operation(summary = "Ứng viên xem lịch sử đã ứng tuyển")
    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Page<ApplicationResponse>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getMyApplications(page, size));
    }

    /**
     * ỨNG VIÊN XEM SỐ LIỆU THỐNG KÊ
     */
    @Operation(summary = "Số liệu thống kê JOB đã ứng tuyển của ứng viên")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CandidateDashboardResponse> getStats() {
        return ResponseEntity.ok(applicationService.getCandidateStats());
    }

    /**
     * EMPLOYER XEM SỐ LIỆU THỐNG KÊ
     */
    @Operation(summary = "Số liệu thống kê JOB và CV của Employer")
    @GetMapping("/employer/stats")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<EmployerDashboardResponse> getEmployerStats() {
        return ResponseEntity.ok(applicationService.getEmployerStats());
    }

    /**
     * EMPLOYER XEM SỐ LIỆU THỐNG KÊ CV THEO THÁNG
     */
    @Operation(summary = "Số liệu thống kê CV đã nộp vào JOB theo tháng")
    @GetMapping("/employer/monthly-stats")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<MonthlyStatResponse>> getMonthlyStats() {
        return ResponseEntity.ok(applicationService.getMonthlyStats());
    }
}