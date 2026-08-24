package com.nuvexa.core.consulta.dto.request;

import com.nuvexa.core.consulta.model.Consulta;
import com.nuvexa.core.consulta.model.StatusConsulta;
import com.nuvexa.core.consulta.model.TipoConsulta;
import com.nuvexa.core.organizacao.model.Organizacao;
import com.nuvexa.core.paciente.model.Paciente;
import jakarta.validation.constraints.NotNull;
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
public class ConsultaCreateRequestDTO {

    @NotNull(message = "{consulta.pacienteId.obrigatorio}")
    private Long pacienteId;

    @NotNull(message = "{consulta.dataHora.obrigatoria}")
    private LocalDateTime dataHora;

    @NotNull(message = "{consulta.duracaoMinutos.obrigatoria}")
    private Integer duracaoMinutos;

    @NotNull(message = "{consulta.tipo.obrigatorio}")
    private TipoConsulta tipo;

    @NotNull(message = "{consulta.status.obrigatorio}")
    private StatusConsulta status;

    private String observacoes;

    public Consulta toConsulta(Organizacao organizacao, Paciente paciente) {
        return Consulta.builder()
                .organizacao(organizacao)
                .paciente(paciente)
                .dataHora(dataHora)
                .duracaoMinutos(duracaoMinutos)
                .tipo(tipo)
                .status(status)
                .observacoes(observacoes)
                .build();
    }
}
