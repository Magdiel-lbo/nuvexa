package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.PacienteProfissional;
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
public class PacienteProfissionalResponseDTO {

    private Long id;

    private Long pacienteId;

    private String pacienteNome;

    private Long profissionalId;

    private String profissionalNome;

    private boolean ativo;

    private LocalDateTime criadoEm;

    public static PacienteProfissionalResponseDTO from(PacienteProfissional pacienteProfissional) {
        return PacienteProfissionalResponseDTO.builder()
                .id(pacienteProfissional.getId())
                .pacienteId(pacienteProfissional.getPaciente().getId())
                .pacienteNome(pacienteProfissional.getPaciente().getNome())
                .profissionalId(pacienteProfissional.getProfissional().getId())
                .profissionalNome(pacienteProfissional.getProfissional().getNome())
                .ativo(pacienteProfissional.isAtivo())
                .criadoEm(pacienteProfissional.getCriadoEm())
                .build();
    }
}
