package com.safelab.platform.shared.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI safeLabOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SafeLab Platform API")
                        .description("Backend API for SafeLab Web Application bounded contexts")
                        .version("1.0.0"))
                .addServersItem(new Server().url("http://localhost:8080").description("Local"));
    }
}
