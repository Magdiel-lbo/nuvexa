package com.nuvexa.core.dto.request;

import com.nuvexa.core.model.Consulta;
import com.nuvexa.core.model.StatusConsulta;
import com.nuvexa.core.model.TipoConsulta;
import com.nuvexa.core.model.Usuario;
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
 * {@code profissionalId} já é o responsável pela consulta e pode ser reatribuído no update
 * (diferente de {@code pacienteId}). Os demais campos são idênticos aos de {@link Consulta},
 * então usa {@code ModelMapper} para eles (exceção documentada na skill nuvexa-backend para
 * atualização campo-a-campo pura) — {@code profissionalId} é resolvido e setado à parte pelo
 * service, sem passar pelo {@code ModelMapper}, já que é um id de relação, não um campo escalar.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaUpdateRequestDTO {

    @NotNull(message = "{consulta.profissionalId.obrigatorio}")
    private Long profissionalId;

    @NotNull(message = "{consulta.dataHora.obrigatoria}")
    private LocalDateTime dataHora;

    @NotNull(message = "{consulta.duracaoMinutos.obrigatoria}")
    private Integer duracaoMinutos;

    @NotNull(message = "{consulta.tipo.obrigatorio}")
    private TipoConsulta tipo;

    @NotNull(message = "{consulta.status.obrigatorio}")
    private StatusConsulta status;

    private String observacoes;

    private String motivo;

    /**
     * Motivo da transição de status (obrigatório quando {@code status} muda para CANCELADA ou
     * FALTOU; ignorado quando o status não muda — validado em {@code ConsultaService}, não aqui,
     * por depender do status anterior). Não confundir com {@link #motivo}, que é o motivo/razão
     * da consulta em si. De propósito fora do {@code ModelMapper.map()}: é lido diretamente pelo
     * service ao montar o histórico, nunca persistido como campo de {@link Consulta}.
     */
    private String motivoTransicao;

    public void atualizar(Consulta consulta, Usuario profissional, ModelMapper modelMapper) {
        modelMapper.map(this, consulta);
        consulta.setProfissional(profissional);
    }
}
