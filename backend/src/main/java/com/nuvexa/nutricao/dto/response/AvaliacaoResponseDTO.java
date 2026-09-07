package com.nuvexa.nutricao.dto.response;

import com.nuvexa.nutricao.model.Avaliacao;
import com.nuvexa.nutricao.model.StatusAvaliacao;
import com.nuvexa.nutricao.model.TipoAvaliacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoResponseDTO {

    private Long id;

    private Long pacienteId;

    private String pacienteNome;

    private Long avaliadorId;

    private String avaliadorNome;

    private LocalDate data;

    private TipoAvaliacao tipo;

    private StatusAvaliacao status;

    private BigDecimal peso;

    private BigDecimal percentualGordura;

    /** Diferença de peso em relação à avaliação concluída anterior do mesmo paciente; nulo se não houver uma. */
    private BigDecimal variacaoPeso;

    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    public static AvaliacaoResponseDTO from(Avaliacao avaliacao, BigDecimal variacaoPeso) {
        return AvaliacaoResponseDTO.builder()
                .id(avaliacao.getId())
                .pacienteId(avaliacao.getPaciente().getId())
                .pacienteNome(avaliacao.getPaciente().getNome())
                .avaliadorId(avaliacao.getAvaliador().getId())
                .avaliadorNome(avaliacao.getAvaliador().getNome())
                .data(avaliacao.getData())
                .tipo(avaliacao.getTipo())
                .status(avaliacao.getStatus())
                .peso(avaliacao.getPeso())
                .percentualGordura(avaliacao.getPercentualGordura())
                .variacaoPeso(variacaoPeso)
                .criadoEm(avaliacao.getCriadoEm())
                .atualizadoEm(avaliacao.getAtualizadoEm())
                .build();
    }
}
