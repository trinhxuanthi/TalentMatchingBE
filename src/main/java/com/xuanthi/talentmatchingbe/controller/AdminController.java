package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.admin.AdminJobResponse;
import com.xuanthi.talentmatchingbe.dto.admin.AdminUserDetailResponse;
import com.xuanthi.talentmatchingbe.dto.admin.AdminUserResponse;
import com.xuanthi.talentmatchingbe.dto.admin.DashboardStatsResponse;
import com.xuanthi.talentmatchingbe.dto.payment.PricingPlanRequest;
import com.xuanthi.talentmatchingbe.dto.payment.PricingPlanResponse;
import com.xuanthi.talentmatchingbe.dto.report.ReportResponse;
import com.xuanthi.talentmatchingbe.entity.AiSetting;
import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.enums.ReportStatus;
import com.xuanthi.talentmatchingbe.enums.Role;
import com.xuanthi.talentmatchingbe.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin") // Giữ prefix chung là /api/admin
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Admin Controller", description = "Các API tổng hợp dành cho Admin quản lý hệ thống")
public class AdminController {

    private final CompanyService companyService;
    private final AdminPricingPlanService adminPricingPlanService;
    private final AdminUserService adminUserService;
    private final AiSettingService aiSettingService;
    private final ReportService reportService;
    private final AdminDashboardService adminDashboardService; // Thêm service thống kê
    private final AdminJobService adminJobService; // Thêm service quản lý Job

    // ==========================================
    // 0. DASHBOARD THỐNG KÊ (PHẦN MỚI)
    // ==========================================
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy dữ liệu thống kê tổng quát")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminDashboardService.getFullAnalytics());
    }

    // ==========================================
    // 1. QUẢN LÝ CÔNG TY (NGUYÊN VĂN FILE 1)
    // ==========================================
    @PatchMapping("/companies/{companyId}/toggle-lock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin Khóa / Mở khóa công ty")
    public ResponseEntity<Map<String, String>> toggleLockCompany(@PathVariable Long companyId) {
        String message = companyService.toggleLockCompany(companyId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // ==========================================
    // 2. QUẢN LÝ GÓI CƯỚC (NGUYÊN VĂN FILE 1)
    // ==========================================
    @GetMapping("/pricing-plans")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xem toàn bộ danh sách gói cước (kể cả đang khóa)")
    public ResponseEntity<List<PricingPlanResponse>> getAllPlans() {
        return ResponseEntity.ok(adminPricingPlanService.getAllPlans());
    }

    @PostMapping("/pricing-plans")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo một gói cước mới")
    public ResponseEntity<PricingPlanResponse> createPlan(@Valid @RequestBody PricingPlanRequest request) {
        return ResponseEntity.ok(adminPricingPlanService.createPlan(request));
    }

    @PutMapping("/pricing-plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật (Tăng/giảm giá, đổi tên, chỉnh % discount)")
    public ResponseEntity<PricingPlanResponse> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody PricingPlanRequest request) {
        return ResponseEntity.ok(adminPricingPlanService.updatePlan(id, request));
    }

    @PatchMapping("/pricing-plans/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bật/Tắt mở bán một gói cước")
    public ResponseEntity<Map<String, String>> toggleStatus(@PathVariable Long id) {
        adminPricingPlanService.togglePlanStatus(id);
        return ResponseEntity.ok(Map.of("message", "Đã thay đổi trạng thái gói cước thành công!"));
    }

    // ==========================================
    // 3. QUẢN LÝ NGƯỜI DÙNG (NGUYÊN VĂN FILE 2 - ADMINUSERCONTROLLER)
    // ==========================================
    @Operation(summary = "Lấy danh sách toàn bộ User (Có bộ lọc siêu tốc)")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        log.debug("Admin is fetching users: keyword={}, role={}, isActive={}", keyword, role, isActive);
        return ResponseEntity.ok(adminUserService.getAllUsers(keyword, role, isActive, page, size));
    }

    @Operation(summary = "Xem chi tiết hồ sơ gốc của 1 User")
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserDetailResponse> getUserDetail(
            @PathVariable @Min(value = 1, message = "ID không hợp lệ") Long id) {

        log.debug("Admin is viewing detail of user ID: {}", id);
        return ResponseEntity.ok(adminUserService.getUserDetail(id));
    }

    @Operation(summary = "Khóa / Mở khóa tài khoản User")
    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> toggleUserStatus(
            @PathVariable @Min(1) Long id,
            @RequestParam boolean isActive,
            @RequestParam(required = false) String reason) {

        log.warn("Admin is changing status of user ID: {} to Active: {}. Reason: {}", id, isActive, reason);
        adminUserService.toggleUserStatus(id, isActive, reason);

        String message = isActive ? "Đã MỞ KHÓA tài khoản thành công!" : "Đã KHÓA tài khoản thành công!";
        return ResponseEntity.ok(Map.of("message", message));
    }

    @Operation(summary = "Cấp quyền hoặc giáng chức User")
    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> changeUserRole(
            @PathVariable @Min(1) Long id,
            @RequestParam Role newRole) {

        log.warn("Admin is changing role of user ID: {} to {}", id, newRole);
        adminUserService.changeUserRole(id, newRole);

        return ResponseEntity.ok(Map.of("message", "Đã cập nhật phân quyền thành công!"));
    }

    @Operation(summary = "Reset mật khẩu khẩn cấp cho User")
    @PostMapping("/users/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> emergencyResetPassword(
            @PathVariable @Min(1) Long id,
            @RequestParam @NotBlank(message = "Vui lòng nhập mật khẩu mới") String newPassword) {

        log.warn("Admin is emergency resetting password for user ID: {}", id);
        adminUserService.emergencyResetPassword(id, newPassword);

        return ResponseEntity.ok(Map.of("message", "Đã reset mật khẩu thành công!"));
    }

    // ==========================================
    // 4. QUẢN LÝ AI SETTINGS (NGUYÊN VĂN FILE 3 - ADMINAICONTROLLER)
    // ==========================================
    @GetMapping("/ai-settings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy cấu hình AI hiện tại từ RAM")
    public ResponseEntity<AiSetting> getAiSettings() {
        log.info("[ADMIN] Truy vấn cấu hình AI toàn cục");
        return ResponseEntity.ok(aiSettingService.getSettings());
    }

    @PutMapping("/ai-settings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật cấu hình AI toàn cục")
    public ResponseEntity<AiSetting> updateAiSettings(@RequestBody AiSetting settings) {
        log.info("[ADMIN] Đang cập nhật lại trọng số AI hệ thống...");
        AiSetting updated = aiSettingService.updateSettings(settings);
        return ResponseEntity.ok(updated);
    }


    @GetMapping("/reports")
    @Operation(summary = "Admin xem danh sách báo cáo")
    public ResponseEntity<Page<ReportResponse>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reportService.getAllReports(status, page, size));
    }

    @PatchMapping("/reports/{id}/resolve")
    @Operation(summary = "Admin xử lý báo cáo")
    public ResponseEntity<Map<String, String>> resolveReport(
            @PathVariable Long id,
            @RequestParam ReportStatus status,
            @RequestParam(required = false) String adminNote) {
        reportService.resolveReport(id, status, adminNote);
        return ResponseEntity.ok(Map.of("message", "Đã xử lý báo cáo!"));
    }

    // ==========================================
    // 6. QUẢN LÝ BÀI TUYỂN DỤNG (JOBS)
    // ==========================================

    @GetMapping("/jobs")
    @Operation(summary = "Admin xem toàn bộ danh sách bài đăng tuyển dụng")
    public ResponseEntity<Page<AdminJobResponse>> getAllJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminJobService.getAllJobs(keyword, status, page, size));
    }

    @PatchMapping("/jobs/{id}/status")
    @Operation(summary = "Admin thay đổi trạng thái Job (Khóa/Mở bài đăng)")
    public ResponseEntity<Map<String, String>> updateJobStatus(
            @PathVariable Long id,
            @RequestParam JobStatus status) {
        adminJobService.updateJobStatus(id, status);
        return ResponseEntity.ok(Map.of("message", "Đã cập nhật trạng thái bài đăng thành công!"));
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Admin xóa vĩnh viễn bài đăng vi phạm")
    public ResponseEntity<Map<String, String>> deleteJob(@PathVariable Long id) {
        adminJobService.deleteJob(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa bài đăng tuyển dụng vĩnh viễn!"));
    }
}