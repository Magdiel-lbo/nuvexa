package com.nuvexa.verticals.nutrition.report.dto.response;

import com.nuvexa.core.patient.model.Gender;
import com.nuvexa.verticals.nutrition.model.ActivityLevel;
import com.nuvexa.verticals.nutrition.model.Goal;
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
public class PatientReportRowDTO {

    private Long id;
    private String name;
    private Integer age;
    private Gender gender;
    private Goal goal;
    private ActivityLevel activityLevel;
    private BigDecimal bmi;
    private String bmiClassification;
    private BigDecimal dailyCalorieExpenditure;
}
