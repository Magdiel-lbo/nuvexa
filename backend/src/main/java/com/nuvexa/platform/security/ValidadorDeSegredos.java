package com.nuvexa.platform.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Impede a subida do profile {@code prod} com segredo ausente, em branco, fraco ou igual ao
 * default público de {@code application.yml}. Só valida; nunca inclui o valor do segredo na
 * mensagem de erro.
 *
 * <p>JWT: {@code JwtService} assina com {@code Keys.hmacShaKeyFor(bytes UTF-8)}, que exige no
 * mínimo 256 bits (32 bytes) para HMAC-SHA. Storage: o {@code S3Client} só exige credencial
 * não nula; o mínimo de 8 caracteres no secret é o do MinIO, o menor entre os backends S3 aceitos.
 */
@Component
@Profile("prod")
public class ValidadorDeSegredos {

    static final int JWT_TAMANHO_MINIMO_BYTES = 32;
    static final int STORAGE_SECRET_TAMANHO_MINIMO = 8;

    private static final Set<String> JWT_PADROES_CONHECIDOS = Set.of(
            "change-this-secret-change-this-secret-change-this-secret");
    private static final Set<String> STORAGE_ACCESS_KEY_PADROES_CONHECIDOS = Set.of("nuvexa");
    private static final Set<String> STORAGE_SECRET_KEY_PADROES_CONHECIDOS = Set.of("nuvexa123");

    public ValidadorDeSegredos(
            @Value("${app.jwt.secret:}") String jwtSecret,
            @Value("${app.storage.access-key:}") String storageAccessKey,
            @Value("${app.storage.secret-key:}") String storageSecretKey) {
        if (estaEmBranco(jwtSecret)) {
            throw falha("app.jwt.secret (JWT_SECRET)", "não foi informado");
        }
        if (JWT_PADROES_CONHECIDOS.contains(jwtSecret)) {
            throw falha("app.jwt.secret (JWT_SECRET)", "usa o valor padrão público");
        }
        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < JWT_TAMANHO_MINIMO_BYTES) {
            throw falha("app.jwt.secret (JWT_SECRET)", "tem menos de " + JWT_TAMANHO_MINIMO_BYTES + " bytes");
        }

        if (estaEmBranco(storageAccessKey)) {
            throw falha("app.storage.access-key (STORAGE_ACCESS_KEY)", "não foi informado");
        }
        if (STORAGE_ACCESS_KEY_PADROES_CONHECIDOS.contains(storageAccessKey)) {
            throw falha("app.storage.access-key (STORAGE_ACCESS_KEY)", "usa o valor padrão público");
        }

        if (estaEmBranco(storageSecretKey)) {
            throw falha("app.storage.secret-key (STORAGE_SECRET_KEY)", "não foi informado");
        }
        if (STORAGE_SECRET_KEY_PADROES_CONHECIDOS.contains(storageSecretKey)) {
            throw falha("app.storage.secret-key (STORAGE_SECRET_KEY)", "usa o valor padrão público");
        }
        if (storageSecretKey.length() < STORAGE_SECRET_TAMANHO_MINIMO) {
            throw falha("app.storage.secret-key (STORAGE_SECRET_KEY)", "tem menos de " + STORAGE_SECRET_TAMANHO_MINIMO + " caracteres");
        }
    }

    private static boolean estaEmBranco(String valor) {
        return valor == null || valor.isBlank();
    }

    private static IllegalStateException falha(String propriedade, String motivo) {
        return new IllegalStateException("Configuração insegura no profile prod: " + propriedade + " " + motivo);
    }
}
