package com.nuvexa.verticals.nutrition.service;

import com.nuvexa.core.patient.model.Gender;
import com.nuvexa.core.patient.model.PatientCore;
import com.nuvexa.core.patient.repository.PatientCoreRepository;
import com.nuvexa.platform.exception.CustomException;
import com.nuvexa.platform.util.EnumOptionResolver;
import com.nuvexa.verticals.nutrition.calculator.BmiCalculator;
import com.nuvexa.verticals.nutrition.calculator.CalorieExpenditureCalculator;
import com.nuvexa.verticals.nutrition.calculator.MetabolicCalculator;
import com.nuvexa.verticals.nutrition.dto.request.PatientCreateRequestDTO;
import com.nuvexa.verticals.nutrition.dto.request.PatientUpdateRequestDTO;
import com.nuvexa.verticals.nutrition.dto.response.PatientEnumsResponseDTO;
import com.nuvexa.verticals.nutrition.dto.response.PatientResponseDTO;
import com.nuvexa.verticals.nutrition.mapper.PatientMapper;
import com.nuvexa.verticals.nutrition.model.ActivityLevel;
import com.nuvexa.verticals.nutrition.model.Goal;
import com.nuvexa.verticals.nutrition.model.NutritionProfile;
import com.nuvexa.verticals.nutrition.model.QNutritionProfile;
import com.nuvexa.verticals.nutrition.repository.NutritionProfileRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class PatientService {

    private static final Locale MESSAGE_LOCALE = Locale.of("pt", "BR");

    private final PatientCoreRepository patientCoreRepository;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final PatientMapper patientMapper;
    private final JPAQueryFactory queryFactory;
    private final MessageSource messageSource;
    private final BmiCalculator bmiCalculator;
    private final MetabolicCalculator metabolicCalculator;
    private final CalorieExpenditureCalculator calorieExpenditureCalculator;

    public PatientResponseDTO create(PatientCreateRequestDTO request) {
        validate(request.getName(), request.getBirthDate(), request.getHeight(), request.getWeight(), request.getManualDailyCalories());

        PatientCore patientCore = patientCoreRepository.save(patientMapper.toPatientCore(request));
        NutritionProfile nutritionProfile = nutritionProfileRepository.save(patientMapper.toNutritionProfile(request, patientCore));
        log.info("Paciente criado com id={}", patientCore.getId());
        return toResponseWithCalculations(nutritionProfile);
    }

    public PatientResponseDTO update(Long id, PatientUpdateRequestDTO request) {
        NutritionProfile nutritionProfile = findProfileOrThrow(id);
        validate(request.getName(), request.getBirthDate(), request.getHeight(), request.getWeight(), request.getManualDailyCalories());

        patientMapper.updatePatientCore(request, nutritionProfile.getPatientCore());
        patientMapper.updateNutritionProfile(request, nutritionProfile);
        patientCoreRepository.save(nutritionProfile.getPatientCore());
        NutritionProfile saved = nutritionProfileRepository.save(nutritionProfile);
        log.info("Paciente atualizado com id={}", id);
        return toResponseWithCalculations(saved);
    }

    public PatientResponseDTO findById(Long id) {
        return toResponseWithCalculations(findProfileOrThrow(id));
    }

    public List<PatientResponseDTO> findAll(String search) {
        return searchProfiles(normalizeSearch(search)).stream()
                .map(this::toResponseWithCalculations)
                .toList();
    }

    public PatientEnumsResponseDTO getEnums() {
        return PatientEnumsResponseDTO.builder()
                .genders(EnumOptionResolver.resolve(Gender.class, "enum.gender", messageSource, MESSAGE_LOCALE))
                .goals(EnumOptionResolver.resolve(Goal.class, "enum.goal", messageSource, MESSAGE_LOCALE))
                .activityLevels(EnumOptionResolver.resolve(ActivityLevel.class, "enum.activityLevel", messageSource, MESSAGE_LOCALE))
                .build();
    }

    public void delete(Long id) {
        NutritionProfile nutritionProfile = findProfileOrThrow(id);
        nutritionProfileRepository.delete(nutritionProfile);
        log.info("Perfil nutricional removido para paciente com id={} (PatientCore preservado)", id);
    }

    private List<NutritionProfile> searchProfiles(String name) {
        QNutritionProfile nutritionProfile = QNutritionProfile.nutritionProfile;

        BooleanExpression nameFilter = name == null ? null : nutritionProfile.patientCore.name.containsIgnoreCase(name);

        return queryFactory
                .selectFrom(nutritionProfile)
                .where(nameFilter)
                .orderBy(nutritionProfile.patientCore.name.asc())
                .fetch();
    }

    private String normalizeSearch(String search) {
        return (search == null || search.isBlank()) ? null : search.trim();
    }

    private NutritionProfile findProfileOrThrow(Long id) {
        return nutritionProfileRepository.findByPatientCoreId(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, resolveMessage("patient.notFound", id)));
    }

    private void validate(String name, LocalDate birthDate, BigDecimal height, BigDecimal weight, BigDecimal manualDailyCalories) {
        if (name == null || name.isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, resolveMessage("patient.name.required"));
        }
        if (birthDate == null || birthDate.isAfter(LocalDate.now())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, resolveMessage("patient.birthDate.future"));
        }
        if (height == null || height.signum() <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, resolveMessage("patient.height.invalid"));
        }
        if (weight == null || weight.signum() <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, resolveMessage("patient.weight.invalid"));
        }
        if (manualDailyCalories != null && manualDailyCalories.signum() <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, resolveMessage("patient.manualDailyCalories.invalid"));
        }
    }

    private String resolveMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, MESSAGE_LOCALE);
    }

    private PatientResponseDTO toResponseWithCalculations(NutritionProfile nutritionProfile) {
        PatientResponseDTO response = patientMapper.toResponse(nutritionProfile);
        PatientCore patientCore = nutritionProfile.getPatientCore();

        int age = Period.between(patientCore.getBirthDate(), LocalDate.now()).getYears();
        BigDecimal bmi = bmiCalculator.calculate(nutritionProfile.getWeight(), nutritionProfile.getHeight());
        BigDecimal bmr = metabolicCalculator.calculate(nutritionProfile.getWeight(), nutritionProfile.getHeight(), age, patientCore.getGender());
        BigDecimal dailyCalorieExpenditure = nutritionProfile.getManualDailyCalories() != null
                ? nutritionProfile.getManualDailyCalories()
                : calorieExpenditureCalculator.calculate(bmr, nutritionProfile.getActivityLevel());

        response.setAge(age);
        response.setBmi(bmi);
        response.setBmiClassification(bmiCalculator.classify(bmi));
        response.setBmr(bmr);
        response.setDailyCalorieExpenditure(dailyCalorieExpenditure);

        return response;
    }
}
