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
    private String name;
    private Integer age;
    private Sexo gender;
    private Objetivo goal;
    private NivelAtividade activityLevel;
    private BigDecimal bmi;
    private String bmiClassification;
    private BigDecimal dailyCalorieExpenditure;
}
