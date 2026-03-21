package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.auth.LoginRequest;
import com.xuanthi.talentmatchingbe.dto.auth.LoginResponse;
import com.xuanthi.talentmatchingbe.dto.auth.RegisterRequest;
import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.Role;
import com.xuanthi.talentmatchingbe.mapper.UserMapper;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    /**
     * Đăng ký người dùng mới (Truyền thống)
     * Tối ưu: Kiểm tra tồn tại trước khi băm mật khẩu để tiết kiệm CPU
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        // TỐI ƯU: Mapper đã lo hết việc gán isActive=true và provider=LOCAL
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    /**
     * Đăng nhập (Truyền thống)
     * Tối ưu: Dùng AuthenticationManager để xử lý logic xác thực tập trung
     */
    public LoginResponse login(LoginRequest request) {
        // Spring Security sẽ tự động kiểm tra email và password ở đây [cite: 2026-03-07]
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .user(userMapper.toUserResponse(user))
                .build();
    }

    /**
     * Xử lý sau khi đăng nhập thành công bằng Facebook/Google
     * Tối ưu: Tự động đăng ký nếu email chưa tồn tại (Just-in-time provisioning)
     */
    @Transactional
    public User processOAuthPostLogin(String email, String name) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .fullName(name)
                            .role(Role.CANDIDATE) // Mặc định cho người dùng mới
                            .password(null) // OAuth2 không cần mật khẩu
                            .build();
                    return userRepository.save(newUser);
                });
    }

    // Sinh mã và gửi mail
    public void requestForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống!"));

        // Sinh 6 số ngẫu nhiên
        String otp = String.format("%06d", new Random().nextInt(1000000));

        user.setResetOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        mailService.sendOtpEmail(email, otp);
    }

    // Xác thực và lưu mật khẩu mới
    @Transactional
    public void verifyAndResetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không hợp lệ!"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp)) {
            throw new RuntimeException("Mã OTP không chính xác!");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn!");
        }

        // Cập nhật pass mới và xóa dấu vết OTP
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String currentEmail, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}