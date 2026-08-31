package com.nuvexa.nutricao.report.dto.request;

import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
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
public class PacienteRelatorioFiltroDTO {

    private String busca;
    private Sexo sexo;
    private Objetivo objetivo;
    private NivelAtividade nivelAtividade;

    public static PacienteRelatorioFiltroDTO of(String busca, Sexo sexo, Objetivo objetivo, NivelAtividade nivelAtividade) {
        return PacienteRelatorioFiltroDTO.builder()
                .busca(busca)
                .sexo(sexo)
                .objetivo(objetivo)
                .nivelAtividade(nivelAtividade)
                .build();
    }
}
