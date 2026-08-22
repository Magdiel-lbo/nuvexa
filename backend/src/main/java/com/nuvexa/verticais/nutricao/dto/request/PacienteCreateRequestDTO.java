package com.nuvexa.verticais.nutricao.dto.request;

import com.nuvexa.verticais.nutricao.model.NivelAtividade;
import com.nuvexa.nucleo.paciente.model.Sexo;
import com.nuvexa.verticais.nutricao.model.Objetivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteCreateRequestDTO {

    @NotBlank(message = "{patient.name.required}")
    private String name;

    @NotNull(message = "{patient.birthDate.required}")
    private LocalDate birthDate;

    @NotNull(message = "{patient.gender.required}")
    private Sexo gender;

    @NotNull(message = "{patient.height.required}")
    private BigDecimal height;

    @NotNull(message = "{patient.weight.required}")
    private BigDecimal weight;

    @NotNull(message = "{patient.goal.required}")
    private Objetivo goal;

    @NotNull(message = "{patient.activityLevel.required}")
    private NivelAtividade activityLevel;

    private BigDecimal manualDailyCalories;

    private String notes;
}
