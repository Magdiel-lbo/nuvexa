package com.nuvexa.verticais.nutricao.relatorio.dto.request;

import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.verticais.nutricao.model.NivelAtividade;
import com.nuvexa.verticais.nutricao.model.Objetivo;
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

    private String search;
    private Sexo gender;
    private Objetivo goal;
    private NivelAtividade activityLevel;
}
