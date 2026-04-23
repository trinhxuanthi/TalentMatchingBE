package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.report.ReportRequest;
import com.xuanthi.talentmatchingbe.service.ReportService;
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Report API")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "Gửi báo cáo/khiếu nại")
    public ResponseEntity<Map<String, String>> submitReport(@Valid @RequestBody ReportRequest request) {
        Long userId = SecurityUtils.getRequiredCurrentUser().getId();
        reportService.createReport(userId, request);
        return ResponseEntity.ok(Map.of("message", "Gửi thành công!"));
    }
}