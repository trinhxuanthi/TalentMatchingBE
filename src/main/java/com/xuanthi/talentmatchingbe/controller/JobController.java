package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.job.JobRequest;
import com.xuanthi.talentmatchingbe.dto.job.JobResponse;
import com.xuanthi.talentmatchingbe.enums.JobType;
import com.xuanthi.talentmatchingbe.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "JobController", description = "Các API dành cho quản lý tin tuyển dụng (Đã tích hợp chuẩn AI)")
public class JobController {

    private final JobService jobService;

    // ==========================================
    // CÁC API DÀNH CHO NHÀ TUYỂN DỤNG (HR)
    // ==========================================

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Tạo mới bài tuyển dụng (Tự động sinh dữ liệu cho AI Python)")
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request) {
        JobResponse response = jobService.createJob(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Cập nhật bài tuyển dụng")
    public ResponseEntity<JobResponse> update(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        // Bổ sung @Valid ở đây để đảm bảo HR sửa bài cũng không bỏ trống kỹ năng của AI
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Xóa bài tuyển dụng")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Xem bài tuyển dụng của mình dành cho EMPLOYER")
    public ResponseEntity<Page<JobResponse>> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getMyJobs(page, size));
    }


    // ==========================================
    // CÁC API PUBLIC (Cho Ứng viên & Khách)
    // ==========================================

    // MỚI BỔ SUNG: API Cực kỳ quan trọng để Frontend vẽ trang Chi tiết Job
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết 1 bài tuyển dụng (Bao gồm Tag kỹ năng Đỏ/Xanh)")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @GetMapping("/public")
    @Operation(summary = "Xem danh sách bài tuyển dụng (Public)")
    public ResponseEntity<Page<JobResponse>> getAllPublicJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getAllPublicJobs(page, size));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm Jobs đa năng")
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<JobResponse> results = jobService.searchJobs(title, location, jobType, minSalary, page, size);
        return ResponseEntity.ok(results);
    }
}