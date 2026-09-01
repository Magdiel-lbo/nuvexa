package com.nuvexa.platform.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

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
}
