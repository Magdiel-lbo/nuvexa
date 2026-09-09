package com.nuvexa.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Transporte interno service → controller para o download — nunca serializado como JSON (o
 * controller monta o {@code ResponseEntity<byte[]>} a partir daqui).
 */
@Getter
@AllArgsConstructor
public class ProntuarioAnexoDownloadDTO {

    private final String nomeOriginal;

    private final String tipoMime;

    private final byte[] conteudo;
}
