package com.nuvexa.nutricao.relatorio.dto.filter;

import com.nuvexa.nutricao.model.QPlanoAlimentar;
import com.nuvexa.nutricao.model.StatusPlanoAlimentar;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoAlimentarFiltro {

    private String busca;
    private List<StatusPlanoAlimentar> status;
    private String nomePlano;
    private List<String> faixaCalorica;
    private List<Integer> refeicoesPorDia;

    public static PlanoAlimentarFiltro of(
            String busca, List<StatusPlanoAlimentar> status, String nomePlano,
            List<String> faixaCalorica, List<Integer> refeicoesPorDia) {
        return PlanoAlimentarFiltro.builder()
                .busca(busca)
                .status(status)
                .nomePlano(nomePlano)
                .faixaCalorica(faixaCalorica)
                .refeicoesPorDia(refeicoesPorDia)
                .build();
    }

    public Predicate toPredicate(QPlanoAlimentar plano, Long organizacaoId) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(plano.organizacao.id.eq(organizacaoId));

        if (busca != null && !busca.isBlank()) {
            String termo = busca.trim();
            predicate.and(plano.nome.containsIgnoreCase(termo).or(plano.paciente.nome.containsIgnoreCase(termo)));
        }
        if (status != null && !status.isEmpty()) {
            predicate.and(plano.status.in(status));
        }
        if (nomePlano != null && !nomePlano.isBlank()) {
            predicate.and(plano.nome.eq(nomePlano));
        }
        if (refeicoesPorDia != null && !refeicoesPorDia.isEmpty()) {
            predicate.and(plano.refeicoesPorDia.in(refeicoesPorDia));
        }

        BooleanExpression faixaExpressao = faixaCaloricaExpressao(plano);
        if (faixaExpressao != null) {
            predicate.and(faixaExpressao);
        }

        return predicate;
    }

    /** Mesmas faixas calculadas no frontend (PlanoAlimentarLista.vue#FAIXAS_CALORICAS). */
    private BooleanExpression faixaCaloricaExpressao(QPlanoAlimentar plano) {
        if (faixaCalorica == null || faixaCalorica.isEmpty()) {
            return null;
        }
        BooleanExpression resultado = null;
        for (String chave : faixaCalorica) {
            BooleanExpression faixa = switch (chave) {
                case "ate1600" -> plano.calorias.loe(1600);
                case "entre1600e2200" -> plano.calorias.gt(1600).and(plano.calorias.loe(2200));
                case "acima2200" -> plano.calorias.gt(2200);
                default -> null;
            };
            if (faixa == null) {
                continue;
            }
            resultado = resultado == null ? faixa : resultado.or(faixa);
        }
        return resultado;
    }
}
