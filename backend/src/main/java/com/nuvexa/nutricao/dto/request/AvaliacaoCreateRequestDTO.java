package com.nuvexa.nutricao.dto.request;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.nutricao.model.Avaliacao;
import com.nuvexa.nutricao.model.StatusAvaliacao;
import com.nuvexa.nutricao.model.TipoAvaliacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoCreateRequestDTO {

    @NotNull(message = "{avaliacao.pacienteId.obrigatorio}")
    private Long pacienteId;

    @NotNull(message = "{avaliacao.avaliadorId.obrigatorio}")
    private Long avaliadorId;

    @NotNull(message = "{avaliacao.data.obrigatoria}")
    private LocalDate data;

    @NotNull(message = "{avaliacao.tipo.obrigatorio}")
    private TipoAvaliacao tipo;

    @NotNull(message = "{avaliacao.status.obrigatorio}")
    private StatusAvaliacao status;

    @Schema(description = "Peso em quilogramas", example = "68.2")
    private BigDecimal peso;

    @Schema(description = "Percentual de gordura corporal (0-100)", example = "18.5")
    private BigDecimal percentualGordura;

    public Avaliacao toAvaliacao(Organizacao organizacao, Paciente paciente, Usuario avaliador) {
        return Avaliacao.builder()
                .organizacao(organizacao)
                .paciente(paciente)
                .avaliador(avaliador)
                .data(data)
                .tipo(tipo)
                .status(status)
                .peso(peso)
                .percentualGordura(percentualGordura)
                .build();
    }
}
