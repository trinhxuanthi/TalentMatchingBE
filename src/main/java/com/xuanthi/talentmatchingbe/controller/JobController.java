package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.job.JobRequest;
import com.xuanthi.talentmatchingbe.dto.job.JobResponse;
import com.xuanthi.talentmatchingbe.enums.JobType;
import com.xuanthi.talentmatchingbe.service.JobService;
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

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Job Controller", description = "Các API dành cho quản lý tin tuyển dụng")
public class JobController {

    private final JobService jobService;

    // ==========================================
    // CÁC API DÀNH CHO NHÀ TUYỂN DỤNG (HR)
    // ==========================================

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Tạo mới bài tuyển dụng")
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request) {
        log.debug("Creating new job: {}", request.getTitle());
        // ✅ Đã xóa try-catch: Nếu request thiếu trường @NotBlank, Handler tự nhả 400
        JobResponse response = jobService.createJob(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Cập nhật bài tuyển dụng")
    public ResponseEntity<JobResponse> update(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        log.debug("Updating job ID: {}", id);
        // ✅ Đã xóa try-catch: Service ném lỗi nếu không tìm thấy ID
        JobResponse response = jobService.updateJob(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Xóa bài tuyển dụng (Soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Deleting job ID: {}", id);
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách các bài tuyển dụng của tôi")
    public ResponseEntity<Page<JobResponse>> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Fetching my jobs - page: {}, size: {}", page, size);
        return ResponseEntity.ok(jobService.getMyJobs(page, size));
    }

    // ==========================================
    // CÁC API PUBLIC (Cho Ứng viên & Khách)
    // ==========================================

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết 1 bài tuyển dụng")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        log.debug("Fetching job detail ID: {}", id);
        // ✅ Handler sẽ tự nhả lỗi 404 kèm message nếu ID không tồn tại
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @GetMapping("/public")
    @Operation(summary = "Xem danh sách bài tuyển dụng công khai")
    public ResponseEntity<Page<JobResponse>> getAllPublicJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Fetching public jobs - page: {}, size: {}", page, size);
        return ResponseEntity.ok(jobService.getAllPublicJobs(page, size));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm việc làm với các bộ lọc")
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Searching jobs - title: {}, location: {}, jobType: {}, minSalary: {}",
                title, location, jobType, minSalary);
        Page<JobResponse> results = jobService.searchJobs(title, location, jobType, minSalary, page, size);
        return ResponseEntity.ok(results);
    }
}