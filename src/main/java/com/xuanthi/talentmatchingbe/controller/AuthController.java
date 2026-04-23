package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.auth.LoginResponse;
import com.xuanthi.talentmatchingbe.dto.auth.RegisterRequest;
import com.xuanthi.talentmatchingbe.dto.auth.LoginRequest;
import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import com.xuanthi.talentmatchingbe.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Auth Controller", description = "APIs for user authentication and password management")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register new user account")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("User registration attempt - email: {}", request.getEmail());
        UserResponse response = authService.register(request);
        log.info("User registered successfully - email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "User login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt - email: {}", request.getEmail());
        LoginResponse response = authService.login(request);
        log.info("Login successful - email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Send OTP for password reset")
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Parameter(description = "User email address", required = true)
            @RequestParam @NotBlank @Email String email) {

        log.info("Password reset request - email: {}", email);
        authService.requestForgotPassword(email);
        return ResponseEntity.ok(Map.of("message", "OTP has been sent to your email!"));
    }

    @Operation(summary = "Reset password with OTP")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam @NotBlank @Email String email,
            @RequestParam @NotBlank String otp,
            @RequestParam @NotBlank @Size(min = 6) String newPassword) {

        log.info("Password reset attempt - email: {}", email);
        authService.verifyAndResetPassword(email, otp, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password has been updated successfully!"));
    }

    @Operation(summary = "Change password for authenticated user")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            Principal principal,
            @RequestParam @NotBlank String oldPassword,
            @RequestParam @NotBlank @Size(min = 6) String newPassword) {

        String email = principal.getName();
        log.info("Password change attempt - email: {}", email);
        authService.changePassword(email, oldPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully!"));
    }
}