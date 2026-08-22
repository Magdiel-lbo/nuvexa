package com.nuvexa.verticals.nutrition.dto.request;

import com.nuvexa.verticals.nutrition.model.ActivityLevel;
import com.nuvexa.core.patient.model.Gender;
import com.nuvexa.verticals.nutrition.model.Goal;
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
public class PatientUpdateRequestDTO {

    @NotBlank(message = "{patient.name.required}")
    private String name;

    @NotNull(message = "{patient.birthDate.required}")
    private LocalDate birthDate;

    @NotNull(message = "{patient.gender.required}")
    private Gender gender;

    @NotNull(message = "{patient.height.required}")
    private BigDecimal height;

    @NotNull(message = "{patient.weight.required}")
    private BigDecimal weight;

    @NotNull(message = "{patient.goal.required}")
    private Goal goal;

    @NotNull(message = "{patient.activityLevel.required}")
    private ActivityLevel activityLevel;

    private BigDecimal manualDailyCalories;

    private String notes;
}
