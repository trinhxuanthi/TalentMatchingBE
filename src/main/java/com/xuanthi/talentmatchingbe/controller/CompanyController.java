package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.company.CompanyDetailResponse;
import com.xuanthi.talentmatchingbe.dto.company.CompanyResponse;
import com.xuanthi.talentmatchingbe.dto.job.JobResponse;
import com.xuanthi.talentmatchingbe.service.CompanyService;
import com.xuanthi.talentmatchingbe.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company Controller", description = "Các API liên quan đến công ty")
public class CompanyController {

    private final CompanyService companyService;
    private final JobService jobService;

    @Operation(summary = "Lấy danh sách tất cả công ty")
    @GetMapping
    public ResponseEntity<Page<CompanyResponse>> getAllCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Fetching all companies - keyword: {}, page: {}, size: {}", keyword, page, size);
        // ✅ Xóa try-catch: Nếu lỗi DB, Handler sẽ tự nhả lỗi 500 kèm message
        Page<CompanyResponse> result = companyService.getAllApprovedCompanies(keyword, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/jobs")
    @Operation(summary = "Lấy danh sách việc làm của công ty đó")
    public ResponseEntity<Page<JobResponse>> getCompanyJobs(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.debug("Fetching jobs for company ID: {} - keyword: {}, page: {}, size: {}", id, keyword, page, size);
        // ✅ Xóa try-catch: Tối giản code cực độ
        Page<JobResponse> result = jobService.getPublicJobsByCompany(id, keyword, page, size);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Xem chi tiết công ty theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDetailResponse> getCompanyDetail(@PathVariable Long id) {
        log.debug("Fetching company detail for ID: {}", id);
        // ✅ Xóa try-catch: Nếu ID không tồn tại, Service ném EntityNotFoundException, Handler hốt gọn
        CompanyDetailResponse result = companyService.getCompanyDetail(id);
        return ResponseEntity.ok(result);
    }
}