package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.auth.LoginResponse;
import com.xuanthi.talentmatchingbe.dto.auth.RegisterRequest;
import com.xuanthi.talentmatchingbe.dto.auth.LoginRequest;
import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import com.xuanthi.talentmatchingbe.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "AuthController", description = "Các API liên quan đến Đăng nhập, Đăng ký và Token")

public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Đăng ký tài khoản")
    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Đăng nhập")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }


    @Operation(summary = "Gửi OTP")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgot(@RequestParam String email) {
        authService.requestForgotPassword(email);
        return ResponseEntity.ok("OTP đã được gửi!");
    }

    @Operation(summary = "Reset mật khẩu")
    @PostMapping("/reset-password")
    public ResponseEntity<String> reset(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        authService.verifyAndResetPassword(email, otp, newPassword);
        return ResponseEntity.ok("Mật khẩu đã được cập nhật thành công!");
    }

    @Operation(summary = "Đổi mật khẩu")
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            Principal principal, // Lấy email từ JWT Token đã đăng nhập
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        String email = principal.getName();
        authService.changePassword(email, oldPassword, newPassword);

        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }
}