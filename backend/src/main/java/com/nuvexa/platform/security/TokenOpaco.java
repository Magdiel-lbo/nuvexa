package com.nuvexa.platform.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/** Token aleatório de uso único enviado por e-mail: guarda-se apenas o hash SHA-256. */
public final class TokenOpaco {

    private static final int BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenOpaco() {
    }

    public static String gerar() {
        byte[] bytes = new byte[BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritmo de hash indisponível", ex);
        }
    }
}
