package com.nuvexa.nutricao.relatorio.dto.filter;

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
import java.util.List;
import java.util.Map;

/**
 * Diferente de {@code ConsultaFiltro}/{@code ProntuarioFiltro}/{@code PlanoAlimentarFiltro}, este
 * filtro não vira {@code Predicate} de QueryDSL: "tendência" (queda/estável/alta) é derivada da
 * variação de peso, que não é coluna — é calculada em memória a partir do histórico completo do
 * paciente (ver AvaliacaoService/AvaliacaoRelatorioService). Por isso a filtragem aqui atua sobre
 * a lista já carregada, depois da variação calculada, não sobre a query.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoFiltro {

    private static final BigDecimal LIMIAR_ESTAVEL = new BigDecimal("0.1");

    private String busca;
    private List<TipoAvaliacao> tipo;
    private List<StatusAvaliacao> status;
    private String periodo;
    private List<Long> avaliadorId;
    private List<String> tendencia;

    public static AvaliacaoFiltro of(
            String busca, List<TipoAvaliacao> tipo, List<StatusAvaliacao> status,
            String periodo, List<Long> avaliadorId, List<String> tendencia) {
        return AvaliacaoFiltro.builder()
                .busca(busca)
                .tipo(tipo)
                .status(status)
                .periodo(periodo)
                .avaliadorId(avaliadorId)
                .tendencia(tendencia)
                .build();
    }

    public boolean aceita(Avaliacao avaliacao, BigDecimal variacaoPeso) {
        if (busca != null && !busca.isBlank() && !avaliacao.getPaciente().getNome().toLowerCase().contains(busca.trim().toLowerCase())) {
            return false;
        }
        if (tipo != null && !tipo.isEmpty() && !tipo.contains(avaliacao.getTipo())) {
            return false;
        }
        if (status != null && !status.isEmpty() && !status.contains(avaliacao.getStatus())) {
            return false;
        }
        if (avaliadorId != null && !avaliadorId.isEmpty() && !avaliadorId.contains(avaliacao.getAvaliador().getId())) {
            return false;
        }
        if (!dentroDoPeriodo(avaliacao.getData())) {
            return false;
        }
        return tendencia == null || tendencia.isEmpty() || tendencia.contains(tendenciaDe(variacaoPeso));
    }

    /** Mesmas janelas de tempo calculadas no frontend (util/periodo.ts#dentroDoPeriodo). */
    private boolean dentroDoPeriodo(LocalDate data) {
        if (periodo == null || periodo.isBlank()) {
            return true;
        }
        LocalDateTime dataHora = data.atStartOfDay();
        LocalDateTime agora = LocalDateTime.now();
        return switch (periodo) {
            case "hoje" -> data.isEqual(LocalDate.now());
            case "7d" -> !dataHora.isBefore(agora.minusDays(7));
            case "30d" -> !dataHora.isBefore(agora.minusDays(30));
            case "90d" -> !dataHora.isBefore(agora.minusDays(90));
            case "ano" -> !dataHora.isBefore(LocalDate.now().withDayOfYear(1).atStartOfDay());
            default -> true;
        };
    }

    private static String tendenciaDe(BigDecimal variacaoPeso) {
        if (variacaoPeso == null) {
            return "estavel";
        }
        if (variacaoPeso.compareTo(LIMIAR_ESTAVEL.negate()) < 0) {
            return "queda";
        }
        if (variacaoPeso.compareTo(LIMIAR_ESTAVEL) > 0) {
            return "alta";
        }
        return "estavel";
    }

    public static String tendenciaDe(Map<Long, BigDecimal> variacaoPorId, Avaliacao avaliacao) {
        return tendenciaDe(variacaoPorId.get(avaliacao.getId()));
    }
}
