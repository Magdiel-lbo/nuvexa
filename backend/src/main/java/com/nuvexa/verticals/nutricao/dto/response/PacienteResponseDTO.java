package com.nuvexa.verticals.nutricao.dto.response;

import com.nuvexa.verticals.nutricao.model.NivelAtividade;
import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.verticals.nutricao.model.Objetivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteResponseDTO {

    private Long id;
    private String name;
    private LocalDate birthDate;
    private Sexo gender;
    private BigDecimal height;
    private BigDecimal weight;
    private Objetivo goal;
    private NivelAtividade activityLevel;
    private BigDecimal manualDailyCalories;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer age;
    private BigDecimal bmi;
    private String bmiClassification;
    private BigDecimal bmr;
    private BigDecimal dailyCalorieExpenditure;
}
