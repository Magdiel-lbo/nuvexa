package com.nuvexa.verticals.nutricao.dto.request;

import com.nuvexa.verticals.nutricao.model.NivelAtividade;
import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.verticals.nutricao.model.Objetivo;
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

    @NotBlank(message = "{paciente.nome.obrigatorio}")
    private String name;

    @NotNull(message = "{paciente.dataNascimento.obrigatoria}")
    private LocalDate birthDate;

    @NotNull(message = "{paciente.sexo.obrigatorio}")
    private Sexo gender;

    @NotNull(message = "{paciente.altura.obrigatoria}")
    private BigDecimal height;

    @NotNull(message = "{paciente.peso.obrigatorio}")
    private BigDecimal weight;

    @NotNull(message = "{paciente.objetivo.obrigatorio}")
    private Objetivo goal;

    @NotNull(message = "{paciente.nivelAtividade.obrigatorio}")
    private NivelAtividade activityLevel;

    private BigDecimal manualDailyCalories;

    private String notes;
}
