package com.xuanthi.talentmatchingbe.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Talent Matching API")
                        .version("1.0")
                        .description("Hệ thống quản lý và gợi ý ứng viên bằng AI"))
                // Khai báo Security Requirement để tất cả API đều hiện hình ổ khóa
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // Định nghĩa chi tiết cách thức gửi Token
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER) // Đảm bảo Token nằm ở Header
                                        .description("Dán JWT Token vào đây (Không cần ghi 'Bearer ')")));
    }
}