package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.user.EmployerUpgradeRequest;
import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import com.xuanthi.talentmatchingbe.dto.user.UserUpdateRequest;
import com.xuanthi.talentmatchingbe.entity.Company;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.service.CompanyService;
import com.xuanthi.talentmatchingbe.service.UploadService;
import com.xuanthi.talentmatchingbe.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "UserController", description = "Các API liên quan đến thông tin ngời dùng")
public class UserController {
    private final UserService userService;
    private final UploadService uploadService;
    private final UserRepository userRepository;
    private final CompanyService companyService;
    @Operation(summary = "Xem thông tin cá nhân")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @Operation(summary = "Thêm hoặc cập nhật avatar")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // consumes = MediaType.MULTIPART_FORM_DATA_VALUE là hiện nu chọn file trên Swagger
    public ResponseEntity<String> updateAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        // Lấy email người dùng hiện tại từ Token
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Upload ảnh lên Cloudinary
        String avatarUrl = uploadService.uploadAvatar(file);

        // Cập nhật URL vào Database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        user.setAvatar(avatarUrl);
        userRepository.save(user);

        return ResponseEntity.ok(avatarUrl);
    }

    @Operation(summary = "Cập nhật thông tin cá nhân")
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @Operation(summary = "Người dùng xin làm Employer")
    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping("/request-upgrade")
    public ResponseEntity<String> requestUpgradeToEmployer(@Valid @RequestBody EmployerUpgradeRequest request) {
        // Chỉ gọi đúng 1 dòng Service
        String result = companyService.requestUpgradeToEmployer(request);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Lấy các yêu cầu đang chờ xét duyệt lên Employer")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<Company>> getPendingCompanies() {
        return ResponseEntity.ok(companyService.getPendingCompanies());
    }

    @Operation(summary = "Duyệt làm Employer")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{companyId}/approve")
    public ResponseEntity<String> approveCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(companyService.approveCompany(companyId));
    }

    @Operation(summary = "Từ chối làm Employer")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{companyId}/reject")
    public ResponseEntity<String> rejectCompany(
            @PathVariable Long companyId,
            @RequestParam String reason) {
        return ResponseEntity.ok(companyService.rejectCompany(companyId, reason));
    }
}
