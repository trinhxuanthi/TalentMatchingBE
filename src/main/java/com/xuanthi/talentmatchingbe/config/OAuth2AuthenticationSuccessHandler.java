package com.xuanthi.talentmatchingbe.config;

import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // Lấy email từ Google/Facebook
        String email = Optional.ofNullable(oauth2User.<String>getAttribute("email"))
                .orElse(oauth2User.getAttribute("id"));

        // 2. TỐI ƯU: Tìm User từ Database để khớp với yêu cầu của JwtServic
        com.xuanthi.talentmatchingbe.entity.User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Nếu chưa có user thì tạo mới (Logic đăng ký nhanh)
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(oauth2User.getAttribute("name"));
                    return userRepository.save(newUser);
                });
        String token = jwtService.generateToken(user);
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}