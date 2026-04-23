package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.company.AdminCompanyResponse;
import com.xuanthi.talentmatchingbe.dto.user.EmployerUpgradeRequest;
import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import com.xuanthi.talentmatchingbe.dto.user.UserUpdateRequest;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.service.CompanyService;
import com.xuanthi.talentmatchingbe.service.UploadService;
import com.xuanthi.talentmatchingbe.service.UserService;
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "User Controller", description = "APIs for user profile and management")
public class UserController {

    private final UserService userService;
    private final UploadService uploadService;
    private final UserRepository userRepository;
    private final CompanyService companyService;

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        log.debug("Get profile request");
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @Operation(summary = "Upload/update user avatar")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SneakyThrows
    public ResponseEntity<Map<String, String>> updateAvatar(
            @Parameter(description = "Avatar image file")
            @RequestParam("file") MultipartFile file) {

        log.debug("Avatar upload request - file: {}", file.getOriginalFilename());

        // ✅ Dùng bùa SecurityUtils: Thay thế 10 dòng lấy Auth rắc rối
        User currentUser = SecurityUtils.getRequiredCurrentUser();

        // Upload image to Cloudinary (Lỗi IO/Cloudinary cứ để Handler lo)
        String avatarUrl = uploadService.uploadAvatar(file);

        // Update URL
        currentUser.setAvatar(avatarUrl);
        userRepository.save(currentUser);

        log.info("Avatar updated successfully for user: {}", currentUser.getEmail());
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        log.debug("Update profile request");
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @Operation(summary = "Request upgrade to employer")
    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping("/request-upgrade")
    public ResponseEntity<Map<String, String>> requestUpgradeToEmployer(@Valid @RequestBody EmployerUpgradeRequest request) {
        log.info("Employer upgrade request from user");
        String result = companyService.requestUpgradeToEmployer(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @Operation(summary = "Get pending company approval requests (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<AdminCompanyResponse>> getPendingCompanies() {
        log.debug("Get pending companies request");
        return ResponseEntity.ok(companyService.getPendingCompanies());
    }

    @Operation(summary = "Approve company request (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{companyId}/approve")
    public ResponseEntity<Map<String, String>> approveCompany(
            @PathVariable @Min(1) Long companyId) {

        log.info("Approve company request for companyId: {}", companyId);
        String result = companyService.approveCompany(companyId);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @Operation(summary = "Reject company request (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{companyId}/reject")
    public ResponseEntity<Map<String, String>> rejectCompany(
            @PathVariable @Min(1) Long companyId,
            @RequestParam String reason) {

        log.info("Reject company request for companyId: {} with reason: {}", companyId, reason);
        String result = companyService.rejectCompany(companyId, reason);
        return ResponseEntity.ok(Map.of("message", result));
    }
}