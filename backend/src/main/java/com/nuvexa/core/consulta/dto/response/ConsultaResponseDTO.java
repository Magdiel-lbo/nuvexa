package com.nuvexa.core.consulta.dto.response;

import com.nuvexa.core.consulta.model.Consulta;
import com.nuvexa.core.consulta.model.StatusConsulta;
import com.nuvexa.core.consulta.model.TipoConsulta;
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

    private LocalDateTime dataHora;

    private Integer duracaoMinutos;

    private TipoConsulta tipo;

    private StatusConsulta status;

    private String observacoes;

    public static ConsultaResponseDTO from(Consulta consulta) {
        return ConsultaResponseDTO.builder()
                .id(consulta.getId())
                .pacienteId(consulta.getPaciente().getId())
                .pacienteNome(consulta.getPaciente().getNome())
                .dataHora(consulta.getDataHora())
                .duracaoMinutos(consulta.getDuracaoMinutos())
                .tipo(consulta.getTipo())
                .status(consulta.getStatus())
                .observacoes(consulta.getObservacoes())
                .build();
    }
}
