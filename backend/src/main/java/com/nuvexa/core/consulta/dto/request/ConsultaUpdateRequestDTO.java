package com.nuvexa.core.consulta.dto.request;

import com.nuvexa.core.consulta.model.Consulta;
import com.nuvexa.core.consulta.model.StatusConsulta;
import com.nuvexa.core.consulta.model.TipoConsulta;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;

/**
 * Sem {@code pacienteId} de propósito: quem a consulta pertence não é editável — só criável.
 * Campos idênticos aos de {@link Consulta}, então usa {@code ModelMapper} (exceção documentada
 * na skill nuvexa-backend para atualização campo-a-campo pura).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaUpdateRequestDTO {

    @NotNull(message = "{consulta.dataHora.obrigatoria}")
    private LocalDateTime dataHora;

    @NotNull(message = "{consulta.duracaoMinutos.obrigatoria}")
    private Integer duracaoMinutos;

    @NotNull(message = "{consulta.tipo.obrigatorio}")
    private TipoConsulta tipo;

    @NotNull(message = "{consulta.status.obrigatorio}")
    private StatusConsulta status;

    private String observacoes;

    public void atualizar(Consulta consulta, ModelMapper modelMapper) {
        modelMapper.map(this, consulta);
    }
}
