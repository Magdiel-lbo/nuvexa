package com.nuvexa.nutricao.relatorio.dto.filter;

import com.nuvexa.core.model.QPaciente;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.nutricao.model.QPerfilNutricional;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteFiltro {

    private String busca;
    private Sexo sexo;
    private Objetivo objetivo;
    private NivelAtividade nivelAtividade;

    public static PacienteFiltro of(String busca, Sexo sexo, Objetivo objetivo, NivelAtividade nivelAtividade) {
        return PacienteFiltro.builder()
                .busca(busca)
                .sexo(sexo)
                .objetivo(objetivo)
                .nivelAtividade(nivelAtividade)
                .build();
    }

    public Predicate toPredicate(QPaciente paciente, QPerfilNutricional perfilNutricional, Long organizacaoId) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(paciente.organizacao.id.eq(organizacaoId));

        if (busca != null && !busca.isBlank()) {
            predicate.and(paciente.nome.containsIgnoreCase(busca.trim()));
        }
        if (sexo != null) {
            predicate.and(paciente.sexo.eq(sexo));
        }
        if (objetivo != null) {
            predicate.and(perfilNutricional.objetivo.eq(objetivo));
        }
        if (nivelAtividade != null) {
            predicate.and(perfilNutricional.nivelAtividade.eq(nivelAtividade));
        }

        return predicate;
    }
}
