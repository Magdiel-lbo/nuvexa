package com.nuvexa.platform.storage;

/**
 * Envolve qualquer falha do provedor de storage (AWS SDK ou outro, no futuro) — quem chama
 * {@link StorageService} nunca precisa conhecer o tipo de exceção do SDK usado por baixo.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
