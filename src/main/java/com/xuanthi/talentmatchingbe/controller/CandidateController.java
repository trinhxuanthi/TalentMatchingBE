package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.user.ProfileViewResponse;
import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import com.xuanthi.talentmatchingbe.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidate API", description = "Các API quản lý ứng viên và hồ sơ (Có bảo mật DTO)")
public class CandidateController {

    private final CandidateService candidateService;

    // ==========================================
    // API DÀNH CHO HR (NHÀ TUYỂN DỤNG)
    // ==========================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "HR xem chi tiết Ứng viên (Hệ thống sẽ tự động ghi log Profile View)")
    public ResponseEntity<UserResponse> getCandidateDetail(@PathVariable Long id) {

        // Trả về thẳng DTO an toàn
        return ResponseEntity.ok(candidateService.getCandidateDetail(id));
    }

    // ==========================================
    // API DÀNH CHO CANDIDATE (ỨNG VIÊN)
    // ==========================================

    @GetMapping("/me/who-viewed")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Ứng viên xem ai đã vào hồ sơ (YÊU CẦU TÀI KHOẢN PRO)")
    public ResponseEntity<Page<ProfileViewResponse>> getWhoViewedMyProfile(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Trả về Page<DTO> chứa tên Công ty và HR
        return ResponseEntity.ok(candidateService.getWhoViewedMyProfile(PageRequest.of(page, size)));
    }

}