    package com.xuanthi.talentmatchingbe.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.DispatcherType;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity // Để @PreAuthorize hoạt động
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Initializing SecurityFilterChain configuration");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        .requestMatchers("/api/auth/change-password").authenticated()
                        // Mở cửa cho Auth API và các luồng OAuth2
                        .requestMatchers(
                                "/api/public/companies/**",
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/login/**",
                                "/oauth2/**",
                                "/auth/**"
                        ).permitAll()
                        .requestMatchers("/api/jobs/public").permitAll()
                        .requestMatchers("/api/jobs/search").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        // Mở cửa cho toàn bộ tài liệu Swagger (Dành cho bản 3.0.2)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-resources/**"
                        ).permitAll()

                        // Cho phép các đường dẫn hệ thống
                        .requestMatchers("/", "/error", "/favicon.ico").permitAll()

                        // Các yêu cầu còn lại bắt buộc phải có Token
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Unauthorized access attempt to: {}", request.getRequestURI());
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\": \"Bạn cần đăng nhập để thực hiện chức năng này!\"}");
                        })
                )
                // --- PHẦN SỬA ĐỂ KHÔNG HIỆN HTML ---
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                // Cấu hình OAuth2 Login cho cả Google và Facebook
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2SuccessHandler)
                )

                // Chế độ Stateless cực kỳ quan trọng cho hiệu năng hệ thống
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("SecurityFilterChain configuration completed successfully");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.debug("Configuring CORS for localhost:3000");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}