package com.xuanthi.talentmatchingbe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(5000); // 5 giây để kết nối
        factory.setReadTimeout(30000);    // 30 giây để chờ AI xử lý (AI thường chạy lâu hơn API thường)

        return new RestTemplate(factory);
    }
}