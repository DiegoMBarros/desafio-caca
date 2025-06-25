package com.transportadora.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server()
            .url("http://localhost:8080")
            .description("Local Server");

        return new OpenAPI()
                .info(new Info()
                        .title("API de Controle de Frota")
                        .version("1.0")
                        .description("API para gerenciamento de frota de caminhões e entregas"))
                .servers(List.of(localServer));
    }
} 