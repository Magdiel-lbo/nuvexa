package com.nuvexa.platform.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Metadados globais da documentação OpenAPI (Swagger UI em /swagger-ui.html,
 * spec em /v3/api-docs). O esquema "bearerAuth" reflete a autenticação JWT
 * usada por toda a API (ver SecurityConfig) e é aplicado por padrão a todos os
 * endpoints; controllers públicos (ex.: AutenticacaoController) sobrescrevem
 * isso com @SecurityRequirement(name = "") no método/classe.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Nuvexa API",
                version = "v1",
                description = "API do Nuvexa, plataforma de gestão para profissionais de saúde e bem-estar."
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    /**
     * Fixa o "server" exibido no Swagger UI/spec em `app.api.base-url` (por
     * profile, ver application*.yml) em vez de deixar o springdoc inferir da
     * própria requisição — necessário quando o backend é acessado por um host
     * (ex.: api.nuvexa.dev) diferente do host interno do container.
     */
    @Bean
    public GlobalOpenApiCustomizer serverUrlCustomizer(@Value("${app.api.base-url}") String baseUrl) {
        return openApi -> openApi.setServers(List.of(new Server().url(baseUrl)));
    }
}
