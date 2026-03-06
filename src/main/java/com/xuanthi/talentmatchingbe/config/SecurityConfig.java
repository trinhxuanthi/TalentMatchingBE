package com.xuanthi.talentmatchingbe.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // Lưu ý: Nếu Frontend bị lỗi CORS, hãy thay bằng cấu hình cụ thể thay vì disable
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Cho phép tất cả các API Auth
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2. Cho phép toàn bộ tài liệu Swagger (Gộp gọn để tránh thiếu sót)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/webjars/**",
                                "/swagger-resources/**"
                        ).permitAll()

                        // 3. Cho phép các đường dẫn hệ thống cơ bản
                        .requestMatchers("/", "/error", "/favicon.ico").permitAll()

                        // 4. Mọi request khác đều phải có Token hợp lệ
                        .anyRequest().authenticated()
                )
                // Kích hoạt đăng nhập Google
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2SuccessHandler)
                )
                // Cấu hình không lưu session (Stateless) cho JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Thêm Filter kiểm tra JWT trước filter xác thực mặc định
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}