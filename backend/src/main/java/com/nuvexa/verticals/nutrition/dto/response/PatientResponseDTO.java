package com.nuvexa.verticals.nutrition.dto.response;

import com.nuvexa.verticals.nutrition.model.ActivityLevel;
import com.nuvexa.core.patient.model.Gender;
import com.nuvexa.verticals.nutrition.model.Goal;
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
public class PatientResponseDTO {

    private Long id;
    private String name;
    private LocalDate birthDate;
    private Gender gender;
    private BigDecimal height;
    private BigDecimal weight;
    private Goal goal;
    private ActivityLevel activityLevel;
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
