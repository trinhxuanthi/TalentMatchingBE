package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.application.*;
import com.xuanthi.talentmatchingbe.entity.Application;
import com.xuanthi.talentmatchingbe.entity.Job;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.ApplicationStatus;
import com.xuanthi.talentmatchingbe.enums.JobStatus;
import com.xuanthi.talentmatchingbe.mapper.ApplicationMapper;
import com.xuanthi.talentmatchingbe.mapper.JobMapper;
import com.xuanthi.talentmatchingbe.repository.ApplicationRepository;
import com.xuanthi.talentmatchingbe.repository.JobRepository;
import com.xuanthi.talentmatchingbe.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final NotificationService notificationService;

    @Transactional
    public String applyJob(ApplicationRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        // TỐI ƯU: Sử dụng Index để check trùng qua jobId từ DTO
        if (applicationRepository.existsByJobIdAndCandidateIdAndIsActiveTrue(request.getJobId(), currentUser.getId())) {
            throw new RuntimeException("Bạn đã nộp đơn cho công việc này rồi!");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Công việc không tồn tại!"));

        Application app = new Application();
        app.setJob(job);
        app.setCandidate(currentUser);
        app.setCvUrl(request.getCvUrl());
        app.setCoverLetter(request.getCoverLetter());
        app.setStatus(ApplicationStatus.PENDING);

        applicationRepository.save(app);
        notificationService.sendNotification(
                job.getEmployer().getId(), // Người nhận là chủ bài đăng
                currentUser.getId(),        // Người gửi là ứng viên
                "Có đơn ứng tuyển mới!",
                "Ứng viên " + currentUser.getFullName() + " vừa nộp đơn vào vị trí " + job.getTitle(),
                "APPLICATION",
                app.getId()                // Để Employer bấm vào là xem được CV ngay
        );

        return "Nộp đơn thành công!";
    }

    @Transactional
    public void updateStatus(Long appId, ApplicationStatus status, String notes) {
        User currentEmployer = SecurityUtils.getCurrentUser();

        Application app = applicationRepository.findByIdAndJobEmployerId(appId, currentEmployer.getId())
                .orElseThrow(() -> new RuntimeException("Bạn không có quyền xử lý đơn này hoặc đơn không tồn tại!"));

        app.setStatus(status);
        if (notes != null) {
            app.setNotes(notes);
        }

        applicationRepository.save(app);

        // Chuyển Enum sang tiếng Việt để thông báo
        String statusVi = switch (status) {
            case REVIEWING -> "Đang được xem xét";
            case INTERVIEW -> "Mời phỏng vấn";
            case ACCEPTED -> "Đã được chấp nhận";
            case REJECTED -> "Đã bị từ chối";
            default -> status.toString();
        };

        notificationService.sendNotification(
                app.getCandidate().getId(),
                currentEmployer.getId(),
                "Cập nhật trạng thái ứng tuyển",
                "Đơn ứng tuyển vào vị trí " + app.getJob().getTitle() + " của bạn " + statusVi,
                "APPLICATION",
                app.getId()
        );
    }


    public Page<ApplicationResponse> getApplicationsByJob(Long jobId, int page, int size) {
        User currentEmployer = SecurityUtils.getCurrentUser();

        // Kiểm tra quyền sở hữu Job trước khi lấy danh sách ứng viên
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng!"));

        if (!job.getEmployer().getId().equals(currentEmployer.getId())) {
            throw new RuntimeException("Bạn không có quyền xem danh sách ứng viên này!");
        }

        //Phân trang + Sắp xếp theo Index (applied_at DESC)
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<Application> applicationPage = applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId, pageable);

        return applicationPage.map(applicationMapper::toResponse);
    }

    public Page<ApplicationResponse> getMyApplications(int page, int size) {
        User currentUser = SecurityUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        return applicationRepository.findByCandidateIdOrderByAppliedAtDesc(currentUser.getId(), pageable)
                .map(applicationMapper::toResponse);
    }

    public CandidateDashboardResponse getCandidateStats() {
        User currentUser = SecurityUtils.getCurrentUser();
        Long userId = currentUser.getId();

        return CandidateDashboardResponse.builder()
                .totalApplied(applicationRepository.countByCandidateId(userId))
                .pendingCount(applicationRepository.countByCandidateIdAndStatus(userId, ApplicationStatus.PENDING))
                .interviewCount(applicationRepository.countByCandidateIdAndStatus(userId, ApplicationStatus.INTERVIEW))
                .acceptedCount(applicationRepository.countByCandidateIdAndStatus(userId, ApplicationStatus.ACCEPTED))
                .rejectedCount(applicationRepository.countByCandidateIdAndStatus(userId, ApplicationStatus.REJECTED))
                .build();
    }

    public EmployerDashboardResponse getEmployerStats() {
        User currentEmployer = SecurityUtils.getCurrentUser();
        Long employerId = currentEmployer.getId();

        // Thống kê tổng quan dựa trên Index (Cực nhanh)
        long activeJobs = jobRepository.countByEmployerIdAndStatus(employerId, JobStatus.OPEN);
        long totalApps = applicationRepository.countByJobEmployerId(employerId);
        long pendingApps = applicationRepository.countByJobEmployerIdAndStatus(employerId, ApplicationStatus.PENDING);
        long interviewApps = applicationRepository.countByJobEmployerIdAndStatus(employerId, ApplicationStatus.INTERVIEW);
        long unreadApps = applicationRepository.countByJobEmployerIdAndIsViewedFalse(employerId);

        // Thống kê sức hút của từng Job (Dùng để vẽ biểu đồ)
        List<Job> myJobs = jobRepository.findAllByEmployerId(employerId);
        Map<String, Long> statsPerJob = myJobs.stream()
                .collect(Collectors.toMap(
                        Job::getTitle,
                        job -> applicationRepository.countByJobId(job.getId()),
                        (existing, replacement) -> existing
                ));

        return EmployerDashboardResponse.builder()
                .totalActiveJobs(activeJobs)
                .totalApplications(totalApps)
                .pendingApplications(pendingApps)
                .unreadApplications(unreadApps) // Đưa vào Response
                .interviewScheduled(interviewApps)
                .applicationsPerJob(statsPerJob)
                .build();
    }

    public List<MonthlyStatResponse> getMonthlyStats() {
        User currentEmployer = SecurityUtils.getCurrentUser();

        List<Object[]> results = applicationRepository.getMonthlyStatsNative(currentEmployer.getId());

        return results.stream()
                .map(result -> new MonthlyStatResponse(
                        (String) result[0],
                        ((Number) result[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    // Employer quản lý trạng thái "Đã xem"
    @Transactional
    public ApplicationResponse getApplicationDetail(Long appId) {
        User currentEmployer = SecurityUtils.getCurrentUser();

        // Sử dụng findByIdAndJobEmployerId để tận dụng Index đã tạo
        Application app = applicationRepository.findByIdAndJobEmployerId(appId, currentEmployer.getId())
                .orElseThrow(() -> new RuntimeException("Đơn ứng tuyển không tồn tại hoặc bạn không có quyền truy cập!"));

        if (!app.isViewed()) {
            app.setViewed(true);
            applicationRepository.save(app);
        }

        return applicationMapper.toResponse(app);
    }



}
