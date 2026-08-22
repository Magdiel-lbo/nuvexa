package com.nuvexa.plataforma.excecao;

import java.time.LocalDateTime;

public record ApiErro(LocalDateTime timestamp, int status, String message, String path) {
}
