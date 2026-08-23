package com.nuvexa.verticals.nutricao.report.dto.response;

import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.verticals.nutricao.model.NivelAtividade;
import com.nuvexa.verticals.nutricao.model.Objetivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteRelatorioLinhaDTO {

    private Long id;
    private String nome;
    private Integer idade;
    private Sexo sexo;
    private Objetivo objetivo;
    private NivelAtividade nivelAtividade;
    private BigDecimal imc;
    private String classificacaoImc;
    private BigDecimal gastoCaloricoDiario;
}
