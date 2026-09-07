package com.nuvexa.core.relatorio.dto.filter;

import com.nuvexa.core.model.QProntuario;
import com.nuvexa.core.model.SecaoProntuario;
import com.nuvexa.core.model.StatusProntuario;
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
public class ProntuarioFiltro {

    private String busca;
    private List<StatusProntuario> status;
    private List<SecaoProntuario> secao;
    private String periodo;
    private List<Long> autorId;
    private List<String> anexo;

    public static ProntuarioFiltro of(
            String busca, List<StatusProntuario> status, List<SecaoProntuario> secao,
            String periodo, List<Long> autorId, List<String> anexo) {
        return ProntuarioFiltro.builder()
                .busca(busca)
                .status(status)
                .secao(secao)
                .periodo(periodo)
                .autorId(autorId)
                .anexo(anexo)
                .build();
    }

    public Predicate toPredicate(QProntuario prontuario, Long organizacaoId) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(prontuario.organizacao.id.eq(organizacaoId));

        if (busca != null && !busca.isBlank()) {
            predicate.and(prontuario.paciente.nome.containsIgnoreCase(busca.trim()));
        }
        if (status != null && !status.isEmpty()) {
            predicate.and(prontuario.status.in(status));
        }
        if (secao != null && !secao.isEmpty()) {
            predicate.and(prontuario.secao.in(secao));
        }
        if (autorId != null && !autorId.isEmpty()) {
            predicate.and(prontuario.autor.id.in(autorId));
        }

        BooleanExpression anexoExpressao = anexoExpressao(prontuario);
        if (anexoExpressao != null) {
            predicate.and(anexoExpressao);
        }

        BooleanExpression periodoExpressao = periodoExpressao(prontuario);
        if (periodoExpressao != null) {
            predicate.and(periodoExpressao);
        }

        return predicate;
    }

    /** Só filtra quando exatamente uma opção está selecionada — as duas juntas equivalem a nenhuma. */
    private BooleanExpression anexoExpressao(QProntuario prontuario) {
        if (anexo == null || anexo.size() != 1) {
            return null;
        }
        return switch (anexo.get(0)) {
            case "sim" -> prontuario.comAnexo.isTrue();
            case "nao" -> prontuario.comAnexo.isFalse();
            default -> null;
        };
    }

    /**
     * Mesmas janelas de tempo calculadas no frontend (util/periodo.ts#dentroDoPeriodo), aplicadas
     * sobre {@code atualizadoEm} — mesmo campo usado por ProntuarioLista.vue para este filtro.
     */
    private BooleanExpression periodoExpressao(QProntuario prontuario) {
        if (periodo == null || periodo.isBlank()) {
            return null;
        }
        LocalDateTime agora = LocalDateTime.now();
        return switch (periodo) {
            case "hoje" -> prontuario.atualizadoEm.between(LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
            case "7d" -> prontuario.atualizadoEm.between(agora.minusDays(7), agora);
            case "30d" -> prontuario.atualizadoEm.between(agora.minusDays(30), agora);
            case "90d" -> prontuario.atualizadoEm.between(agora.minusDays(90), agora);
            case "ano" -> prontuario.atualizadoEm.between(LocalDate.now().withDayOfYear(1).atStartOfDay(), agora);
            default -> null;
        };
    }
}
