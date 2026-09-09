package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.ProntuarioAdendo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProntuarioAdendoResponseDTO {

    private Long id;

    private Long prontuarioId;

    private Long autorId;

    private String autorNome;

    private String texto;

    private LocalDateTime criadoEm;

    public static ProntuarioAdendoResponseDTO from(ProntuarioAdendo adendo) {
        return ProntuarioAdendoResponseDTO.builder()
                .id(adendo.getId())
                .prontuarioId(adendo.getProntuario().getId())
                .autorId(adendo.getAutor().getId())
                .autorNome(adendo.getAutor().getNome())
                .texto(adendo.getTexto())
                .criadoEm(adendo.getCriadoEm())
                .build();
    }
}
