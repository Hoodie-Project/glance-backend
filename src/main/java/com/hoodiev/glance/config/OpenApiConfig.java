package com.hoodiev.glance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI glanceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Glance API")
                .description("위치 기반 익명 스레드 서비스 API")
                .version("v0.1"));
    }
}
