package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.company.CompanyResponse;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.service.CompanyService; // Sửa import này
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/candidate/companies")
@RequiredArgsConstructor
@Tag(name = "Company Follow Controller", description = "Tính năng Theo dõi công ty")
public class CompanyFollowController {

    // ✅ Đổi FollowService thành CompanyService
    private final CompanyService companyService;

    @PostMapping("/{companyId}/toggle-follow")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Nhấn để Follow/Unfollow công ty")
    public ResponseEntity<Map<String, String>> toggleFollow(@PathVariable Long companyId) {
        User currentUser = SecurityUtils.getRequiredCurrentUser();
        String message = companyService.toggleFollow(currentUser.getId(), companyId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping("/following")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Xem danh sách các công ty đang theo dõi")
    public ResponseEntity<Page<CompanyResponse>> getFollowing(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = SecurityUtils.getRequiredCurrentUser();
        return ResponseEntity.ok(companyService.getFollowingCompanies(currentUser.getId(), page, size));
    }
}