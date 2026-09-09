package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.ProntuarioAnexo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * {@code chaveStorage} de propósito não aparece aqui — o frontend nunca deve saber onde/como o
 * arquivo é guardado, só baixa pelo endpoint dedicado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProntuarioAnexoResponseDTO {

    private Long id;

    private String nomeOriginal;

    private String tipoMime;

    private Long tamanho;

    private Long criadoPorId;

    private String criadoPorNome;

    private LocalDateTime criadoEm;

    public static ProntuarioAnexoResponseDTO from(ProntuarioAnexo anexo) {
        return ProntuarioAnexoResponseDTO.builder()
                .id(anexo.getId())
                .nomeOriginal(anexo.getNomeOriginal())
                .tipoMime(anexo.getTipoMime())
                .tamanho(anexo.getTamanho())
                .criadoPorId(anexo.getCriadoPor().getId())
                .criadoPorNome(anexo.getCriadoPor().getNome())
                .criadoEm(anexo.getCriadoEm())
                .build();
    }
}
