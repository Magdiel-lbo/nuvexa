package com.nuvexa.verticals.nutrition.report.mapper;

import com.nuvexa.core.patient.model.Gender;
import com.nuvexa.core.patient.model.PatientCore;
import com.nuvexa.platform.dto.EnumOptionDTO;
import com.nuvexa.platform.report.ReportColumn;
import com.nuvexa.platform.util.EnumOptionResolver;
import com.nuvexa.verticals.nutrition.model.ActivityLevel;
import com.nuvexa.verticals.nutrition.model.Goal;
import com.nuvexa.verticals.nutrition.model.NutritionProfile;
import com.nuvexa.verticals.nutrition.report.dto.response.PatientReportRowDTO;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PatientReportMapper {

    public List<ReportColumn<PatientReportRowDTO>> buildColumns(MessageSource messageSource, Locale locale) {
        Map<String, String> genderLabels = labelMap(EnumOptionResolver.resolve(Gender.class, "enum.gender", messageSource, locale));
        Map<String, String> goalLabels = labelMap(EnumOptionResolver.resolve(Goal.class, "enum.goal", messageSource, locale));
        Map<String, String> activityLevelLabels = labelMap(
                EnumOptionResolver.resolve(ActivityLevel.class, "enum.activityLevel", messageSource, locale));

        return List.of(
                column("name", "report.patient.column.name", 1, messageSource, locale, PatientReportRowDTO::getName),
                column("age", "report.patient.column.age", 2, messageSource, locale, PatientReportRowDTO::getAge),
                column("gender", "report.patient.column.gender", 3, messageSource, locale,
                        row -> genderLabels.get(row.getGender().name())),
                column("goal", "report.patient.column.goal", 4, messageSource, locale,
                        row -> goalLabels.get(row.getGoal().name())),
                column("activityLevel", "report.patient.column.activityLevel", 5, messageSource, locale,
                        row -> activityLevelLabels.get(row.getActivityLevel().name())),
                column("bmi", "report.patient.column.bmi", 6, messageSource, locale, PatientReportRowDTO::getBmi),
                column("bmiClassification", "report.patient.column.bmiClassification", 7, messageSource, locale,
                        PatientReportRowDTO::getBmiClassification),
                column("dailyCalorieExpenditure", "report.patient.column.dailyCalorieExpenditure", 8, messageSource, locale,
                        PatientReportRowDTO::getDailyCalorieExpenditure));
    }

    public PatientReportRowDTO toRow(
            NutritionProfile nutritionProfile, int age, BigDecimal bmi, String bmiClassification, BigDecimal dailyCalorieExpenditure) {
        PatientCore patientCore = nutritionProfile.getPatientCore();
        return PatientReportRowDTO.builder()
                .id(patientCore.getId())
                .name(patientCore.getName())
                .age(age)
                .gender(patientCore.getGender())
                .goal(nutritionProfile.getGoal())
                .activityLevel(nutritionProfile.getActivityLevel())
                .bmi(bmi)
                .bmiClassification(bmiClassification)
                .dailyCalorieExpenditure(dailyCalorieExpenditure)
                .build();
    }

    private Map<String, String> labelMap(List<EnumOptionDTO> options) {
        return options.stream().collect(Collectors.toMap(EnumOptionDTO::getValue, EnumOptionDTO::getLabel));
    }

    private ReportColumn<PatientReportRowDTO> column(
            String key, String messageKey, int order, MessageSource messageSource, Locale locale,
            Function<PatientReportRowDTO, Object> valueExtractor) {
        return ReportColumn.of(key, messageSource.getMessage(messageKey, null, locale), order, valueExtractor);
    }
}
