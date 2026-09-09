package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.Consulta;
import com.nuvexa.core.model.StatusConsulta;
import com.nuvexa.core.model.TipoConsulta;
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
public class ConsultaResponseDTO {

    private Long id;

    private Long pacienteId;

    private String pacienteNome;

    private Long profissionalId;

    private String profissionalNome;

    private LocalDateTime dataHora;

    private Integer duracaoMinutos;

    private TipoConsulta tipo;

    private StatusConsulta status;

    private String observacoes;

    private String motivo;

    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    public static ConsultaResponseDTO from(Consulta consulta) {
        return ConsultaResponseDTO.builder()
                .id(consulta.getId())
                .pacienteId(consulta.getPaciente().getId())
                .pacienteNome(consulta.getPaciente().getNome())
                .profissionalId(consulta.getProfissional().getId())
                .profissionalNome(consulta.getProfissional().getNome())
                .dataHora(consulta.getDataHora())
                .duracaoMinutos(consulta.getDuracaoMinutos())
                .tipo(consulta.getTipo())
                .status(consulta.getStatus())
                .observacoes(consulta.getObservacoes())
                .motivo(consulta.getMotivo())
                .criadoEm(consulta.getCriadoEm())
                .atualizadoEm(consulta.getAtualizadoEm())
                .build();
    }
}
