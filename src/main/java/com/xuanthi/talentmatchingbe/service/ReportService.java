package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.report.ReportRequest;
import com.xuanthi.talentmatchingbe.dto.report.ReportResponse;
import com.xuanthi.talentmatchingbe.entity.Report;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.ReportStatus;
import com.xuanthi.talentmatchingbe.repository.ReportRepository;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createReport(Long userId, ReportRequest request) {
        User sender = userRepository.findById(userId).orElseThrow();
        Report report = Report.builder()
                .sender(sender)
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType())
                .build();
        reportRepository.save(report);
    }

    @Transactional
    public void resolveReport(Long reportId, ReportStatus status, String adminNote) {
        Report report = reportRepository.findById(reportId).orElseThrow();
        report.setStatus(status);
        report.setAdminNote(adminNote);
        reportRepository.save(report);
    }

    public Page<ReportResponse> getAllReports(ReportStatus status, int page, int size) {
        return reportRepository.findAllByStatus(status, PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .senderEmail(report.getSender().getEmail())
                .title(report.getTitle())
                .content(report.getContent())
                .type(report.getType())
                .status(report.getStatus())
                .adminNote(report.getAdminNote())
                .createdAt(report.getCreatedAt())
                .build();
    }
}