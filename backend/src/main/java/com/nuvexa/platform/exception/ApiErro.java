package com.nuvexa.platform.exception;

import java.time.LocalDateTime;

public record ApiErro(LocalDateTime dataHora, int status, String mensagem, String caminho) {
}
