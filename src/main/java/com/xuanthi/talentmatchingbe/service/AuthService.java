package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.auth.LoginRequest;
import com.xuanthi.talentmatchingbe.dto.auth.LoginResponse;
import com.xuanthi.talentmatchingbe.dto.auth.RegisterRequest;
import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.Role;
import com.xuanthi.talentmatchingbe.mapper.UserMapper;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    /**
     * Đăng ký người dùng mới (Truyền thống)
     * Tối ưu: Kiểm tra tồn tại trước khi băm mật khẩu để tiết kiệm CPU
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .build();

        return userMapper.toResponse(userRepository.save(user));
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

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .user(userMapper.toResponse(user))
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
}