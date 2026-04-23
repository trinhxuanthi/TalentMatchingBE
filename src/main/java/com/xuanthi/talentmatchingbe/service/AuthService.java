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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Đăng ký người dùng mới (Truyền thống)
     * Tối ưu: Kiểm tra tồn tại trước khi băm mật khẩu để tiết kiệm CPU
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new RuntimeException("Dữ liệu đăng ký không hợp lệ!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Đăng ký thất bại: Email {} đã tồn tại", request.getEmail());
            throw new RuntimeException("Email đã tồn tại!");
        }

        // TỐI ƯU: Mapper đã lo hết việc gán isActive=true và provider=LOCAL
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("Đăng ký thành công cho email: {}", request.getEmail());
        return userMapper.toUserResponse(savedUser);
    }

    /**
     * Đăng nhập (Truyền thống)
     * Tối ưu: Dùng AuthenticationManager để xử lý logic xác thực tập trung
     */
    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new RuntimeException("Dữ liệu đăng nhập không hợp lệ!");
        }

        // Spring Security sẽ tự động kiểm tra email và password ở đây
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại hoặc chưa kích hoạt"));

        String token = jwtService.generateToken(user);
        log.info("Đăng nhập thành công cho email: {}", request.getEmail());

        return LoginResponse.builder()
                .token(token)
                .user(userMapper.toUserResponse(user))
                .urlAvatarCompany(user.getCompany() != null ? user.getCompany().getLogoUrl() : null)
                .idCompany(user.getCompany() != null ? user.getCompany().getId() : null) // Tránh NullPointer nếu user không có company
                .build();
    }

    /**
     * Xử lý sau khi đăng nhập thành công bằng Facebook/Google
     * Tối ưu: Tự động đăng ký nếu email chưa tồn tại (Just-in-time provisioning)
     */
    @Transactional
    public User processOAuthPostLogin(String email, String name) {
        if (email == null || name == null) {
            throw new RuntimeException("Dữ liệu OAuth không hợp lệ!");
        }

        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .fullName(name)
                            .role(Role.CANDIDATE) // Mặc định cho người dùng mới
                            .password(null) // OAuth2 không cần mật khẩu
                            .isActive(true) // Kích hoạt ngay cho OAuth
                            .provider("OAUTH2") // Phân biệt với LOCAL
                            .build();
                    User savedUser = userRepository.save(newUser);
                    log.info("Tạo user OAuth thành công cho email: {}", email);
                    return savedUser;
                });
    }

    // Sinh mã và gửi mail
    public void requestForgotPassword(String email) {
        if (email == null) {
            throw new RuntimeException("Email không được để trống!");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống!"));

        // Sinh 6 số ngẫu nhiên an toàn
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1000000));

        user.setResetOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        try {
            mailService.sendOtpEmail(email, otp);
            log.info("Gửi OTP thành công cho email: {}", email);
        } catch (Exception e) {
            log.error("Gửi OTP thất bại cho email: {}", email, e);
            throw new RuntimeException("Không thể gửi email OTP!");
        }
    }

    // Xác thực và lưu mật khẩu mới
    @Transactional
    public void verifyAndResetPassword(String email, String otp, String newPassword) {
        if (email == null || otp == null || newPassword == null) {
            throw new RuntimeException("Dữ liệu không hợp lệ!");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không hợp lệ!"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp)) {
            log.warn("OTP không chính xác cho email: {}", email);
            throw new RuntimeException("Mã OTP không chính xác!");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            log.warn("OTP hết hạn cho email: {}", email);
            throw new RuntimeException("Mã OTP đã hết hạn!");
        }

        // Cập nhật pass mới và xóa dấu vết OTP
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        log.info("Reset password thành công cho email: {}", email);
    }

    @Transactional
    public void changePassword(String currentEmail, String oldPassword, String newPassword) {
        if (currentEmail == null || oldPassword == null || newPassword == null) {
            throw new RuntimeException("Dữ liệu không hợp lệ!");
        }

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Mật khẩu cũ không chính xác cho email: {}", currentEmail);
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Đổi password thành công cho email: {}", currentEmail);
    }
}