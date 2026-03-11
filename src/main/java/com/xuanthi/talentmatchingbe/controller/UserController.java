package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import com.xuanthi.talentmatchingbe.dto.user.UserUpdateRequest;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.service.UploadService;
import com.xuanthi.talentmatchingbe.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "UserController", description = "Các API liên quan đến thông tin ngời dùng")
public class UserController {
    private final UserService userService;
    private final UploadService uploadService;
    private final UserRepository userRepository;
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
}
