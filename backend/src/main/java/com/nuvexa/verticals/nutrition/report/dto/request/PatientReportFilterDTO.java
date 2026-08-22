package com.nuvexa.verticals.nutrition.report.dto.request;

import com.nuvexa.core.patient.model.Gender;
import com.nuvexa.verticals.nutrition.model.ActivityLevel;
import com.nuvexa.verticals.nutrition.model.Goal;
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
public class PatientReportFilterDTO {

    private String search;
    private Gender gender;
    private Goal goal;
    private ActivityLevel activityLevel;
}
