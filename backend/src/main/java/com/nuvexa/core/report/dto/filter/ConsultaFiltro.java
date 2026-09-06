package com.nuvexa.core.report.dto.filter;

import com.nuvexa.core.model.QConsulta;
import com.nuvexa.core.model.StatusConsulta;
import com.nuvexa.core.model.TipoConsulta;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaFiltro {

    private String busca;
    private List<StatusConsulta> status;
    private List<TipoConsulta> tipo;
    private String periodo;
    private List<Long> profissionalId;

    public static ConsultaFiltro of(
            String busca, List<StatusConsulta> status, List<TipoConsulta> tipo, String periodo, List<Long> profissionalId) {
        return ConsultaFiltro.builder()
                .busca(busca)
                .status(status)
                .tipo(tipo)
                .periodo(periodo)
                .profissionalId(profissionalId)
                .build();
    }

    public Predicate toPredicate(QConsulta consulta, Long organizacaoId) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(consulta.organizacao.id.eq(organizacaoId));

        if (busca != null && !busca.isBlank()) {
            predicate.and(consulta.paciente.nome.containsIgnoreCase(busca.trim()));
        }
        if (status != null && !status.isEmpty()) {
            predicate.and(consulta.status.in(status));
        }
        if (tipo != null && !tipo.isEmpty()) {
            predicate.and(consulta.tipo.in(tipo));
        }
        if (profissionalId != null && !profissionalId.isEmpty()) {
            predicate.and(consulta.profissional.id.in(profissionalId));
        }

        BooleanExpression periodoExpressao = periodoExpressao(consulta);
        if (periodoExpressao != null) {
            predicate.and(periodoExpressao);
        }

        return predicate;
    }

    /**
     * Mesmas janelas de tempo calculadas no frontend (util/periodo.ts#dentroDoPeriodo) —
     * mantidas em sincronia para que o Excel reflita exatamente o que a tela mostra.
     */
    private BooleanExpression periodoExpressao(QConsulta consulta) {
        if (periodo == null || periodo.isBlank()) {
            return null;
        }
        LocalDateTime agora = LocalDateTime.now();
        return switch (periodo) {
            case "hoje" -> consulta.dataHora.between(LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
            case "7d" -> consulta.dataHora.between(agora.minusDays(7), agora);
            case "30d" -> consulta.dataHora.between(agora.minusDays(30), agora);
            case "90d" -> consulta.dataHora.between(agora.minusDays(90), agora);
            case "ano" -> consulta.dataHora.between(LocalDate.now().withDayOfYear(1).atStartOfDay(), agora);
            default -> null;
        };
    }
}
