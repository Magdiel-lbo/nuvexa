package com.nuvexa.verticals.nutrition.report.service;

import com.nuvexa.core.patient.model.QPatientCore;
import com.nuvexa.platform.report.ReportColumn;
import com.nuvexa.platform.report.ReportResponseDTO;
import com.nuvexa.platform.report.excel.ExcelReportWriter;
import com.nuvexa.verticals.nutrition.calculator.BmiCalculator;
import com.nuvexa.verticals.nutrition.calculator.CalorieExpenditureCalculator;
import com.nuvexa.verticals.nutrition.calculator.MetabolicCalculator;
import com.nuvexa.verticals.nutrition.model.NutritionProfile;
import com.nuvexa.verticals.nutrition.model.QNutritionProfile;
import com.nuvexa.verticals.nutrition.report.dto.request.PatientReportFilterDTO;
import com.nuvexa.verticals.nutrition.report.dto.response.PatientReportRowDTO;
import com.nuvexa.verticals.nutrition.report.mapper.PatientReportMapper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PatientReportService {

    private static final Locale MESSAGE_LOCALE = Locale.of("pt", "BR");

    private final JPAQueryFactory queryFactory;
    private final MessageSource messageSource;
    private final PatientReportMapper patientReportMapper;
    private final BmiCalculator bmiCalculator;
    private final MetabolicCalculator metabolicCalculator;
    private final CalorieExpenditureCalculator calorieExpenditureCalculator;

    @Transactional(readOnly = true)
    public ReportResponseDTO<PatientReportRowDTO> generate(PatientReportFilterDTO filter) {
        return ReportResponseDTO.of(columns(), rows(filter));
    }

    @Transactional(readOnly = true)
    public byte[] generateExcel(PatientReportFilterDTO filter) {
        return ExcelReportWriter.write("Pacientes", columns(), rows(filter));
    }

    private List<ReportColumn<PatientReportRowDTO>> columns() {
        return patientReportMapper.buildColumns(messageSource, MESSAGE_LOCALE);
    }

    private List<PatientReportRowDTO> rows(PatientReportFilterDTO filter) {
        return searchProfiles(filter).stream().map(this::toRow).toList();
    }

    private List<NutritionProfile> searchProfiles(PatientReportFilterDTO filter) {
        QNutritionProfile nutritionProfile = QNutritionProfile.nutritionProfile;
        QPatientCore patientCore = QPatientCore.patientCore;

        BooleanExpression searchFilter = filter.getSearch() == null || filter.getSearch().isBlank()
                ? null
                : patientCore.name.containsIgnoreCase(filter.getSearch().trim());
        BooleanExpression genderFilter = filter.getGender() == null ? null : patientCore.gender.eq(filter.getGender());
        BooleanExpression goalFilter = filter.getGoal() == null ? null : nutritionProfile.goal.eq(filter.getGoal());
        BooleanExpression activityLevelFilter = filter.getActivityLevel() == null
                ? null
                : nutritionProfile.activityLevel.eq(filter.getActivityLevel());

        return queryFactory
                .selectFrom(nutritionProfile)
                .join(nutritionProfile.patientCore, patientCore)
                .where(searchFilter, genderFilter, goalFilter, activityLevelFilter)
                .orderBy(patientCore.name.asc())
                .fetch();
    }

    private PatientReportRowDTO toRow(NutritionProfile nutritionProfile) {
        int age = Period.between(nutritionProfile.getPatientCore().getBirthDate(), LocalDate.now()).getYears();
        BigDecimal bmi = bmiCalculator.calculate(nutritionProfile.getWeight(), nutritionProfile.getHeight());
        BigDecimal bmr = metabolicCalculator.calculate(
                nutritionProfile.getWeight(), nutritionProfile.getHeight(), age, nutritionProfile.getPatientCore().getGender());
        BigDecimal dailyCalorieExpenditure = nutritionProfile.getManualDailyCalories() != null
                ? nutritionProfile.getManualDailyCalories()
                : calorieExpenditureCalculator.calculate(bmr, nutritionProfile.getActivityLevel());

        return patientReportMapper.toRow(nutritionProfile, age, bmi, bmiCalculator.classify(bmi), dailyCalorieExpenditure);
    }
}
