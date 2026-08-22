package com.nuvexa.verticals.nutrition.service;

import com.nuvexa.core.patient.model.Gender;
import com.nuvexa.core.patient.model.PatientCore;
import com.nuvexa.core.patient.repository.PatientCoreRepository;
import com.nuvexa.platform.exception.CustomException;
import com.nuvexa.verticals.nutrition.calculator.BmiCalculator;
import com.nuvexa.verticals.nutrition.calculator.CalorieExpenditureCalculator;
import com.nuvexa.verticals.nutrition.calculator.MetabolicCalculator;
import com.nuvexa.verticals.nutrition.dto.request.PatientCreateRequestDTO;
import com.nuvexa.verticals.nutrition.dto.request.PatientUpdateRequestDTO;
import com.nuvexa.verticals.nutrition.dto.response.PatientResponseDTO;
import com.nuvexa.verticals.nutrition.mapper.PatientMapper;
import com.nuvexa.verticals.nutrition.model.ActivityLevel;
import com.nuvexa.verticals.nutrition.model.Goal;
import com.nuvexa.verticals.nutrition.model.NutritionProfile;
import com.nuvexa.verticals.nutrition.repository.NutritionProfileRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PatientServiceTest {

    @Mock
    private PatientCoreRepository patientCoreRepository;

    @Mock
    private NutritionProfileRepository nutritionProfileRepository;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private MessageSource messageSource;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(messageSource.getMessage(any(String.class), any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(patientCoreRepository.save(any(PatientCore.class))).thenAnswer(invocation -> {
            PatientCore patientCore = invocation.getArgument(0);
            if (patientCore.getId() == null) {
                patientCore.setId(1L);
            }
            return patientCore;
        });
        when(nutritionProfileRepository.save(any(NutritionProfile.class))).thenAnswer(invocation -> {
            NutritionProfile profile = invocation.getArgument(0);
            if (profile.getId() == null) {
                profile.setId(100L);
            }
            return profile;
        });
        patientService = new PatientService(
                patientCoreRepository,
                nutritionProfileRepository,
                new PatientMapper(),
                queryFactory,
                messageSource,
                new BmiCalculator(messageSource),
                new MetabolicCalculator(),
                new CalorieExpenditureCalculator());
    }

    private PatientCreateRequestDTO validCreateRequest() {
        PatientCreateRequestDTO request = new PatientCreateRequestDTO();
        request.setName("Maria Souza");
        request.setBirthDate(LocalDate.of(1990, 5, 20));
        request.setGender(Gender.FEMALE);
        request.setHeight(new BigDecimal("1.65"));
        request.setWeight(new BigDecimal("62.50"));
        request.setGoal(Goal.LOSE_WEIGHT);
        request.setActivityLevel(ActivityLevel.MODERATELY_ACTIVE);
        return request;
    }

    @Test
    void shouldCreatePatientWhenDataIsValid() {
        PatientResponseDTO response = patientService.create(validCreateRequest());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Maria Souza");
    }

    static Stream<Arguments> invalidCreateRequests() {
        return Stream.of(
                Arguments.of("blank name", (java.util.function.Consumer<PatientCreateRequestDTO>) r -> r.setName("  ")),
                Arguments.of("null name", (java.util.function.Consumer<PatientCreateRequestDTO>) r -> r.setName(null)),
                Arguments.of("future birthDate", (java.util.function.Consumer<PatientCreateRequestDTO>) r -> r.setBirthDate(LocalDate.now().plusDays(1))),
                Arguments.of("zero height", (java.util.function.Consumer<PatientCreateRequestDTO>) r -> r.setHeight(BigDecimal.ZERO)),
                Arguments.of("negative height", (java.util.function.Consumer<PatientCreateRequestDTO>) r -> r.setHeight(new BigDecimal("-1.70"))),
                Arguments.of("zero weight", (java.util.function.Consumer<PatientCreateRequestDTO>) r -> r.setWeight(BigDecimal.ZERO)),
                Arguments.of("negative weight", (java.util.function.Consumer<PatientCreateRequestDTO>) r -> r.setWeight(new BigDecimal("-60"))),
                Arguments.of("zero manualDailyCalories", (java.util.function.Consumer<PatientCreateRequestDTO>) r -> r.setManualDailyCalories(BigDecimal.ZERO)),
                Arguments.of("negative manualDailyCalories", (java.util.function.Consumer<PatientCreateRequestDTO>) r -> r.setManualDailyCalories(new BigDecimal("-500")))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateRequests")
    void shouldRejectInvalidPatientData(String scenario, java.util.function.Consumer<PatientCreateRequestDTO> mutator) {
        PatientCreateRequestDTO request = validCreateRequest();
        mutator.accept(request);

        assertThatThrownBy(() -> patientService.create(request))
                .isInstanceOf(CustomException.class);

        verify(patientCoreRepository, never()).save(any());
        verify(nutritionProfileRepository, never()).save(any());
    }

    @Test
    void shouldPopulateCalculatedFieldsOnCreate() {
        PatientCreateRequestDTO request = validCreateRequest();
        request.setBirthDate(LocalDate.now().minusYears(30));
        request.setWeight(new BigDecimal("80"));
        request.setHeight(new BigDecimal("1.80"));
        request.setGender(Gender.MALE);
        request.setActivityLevel(ActivityLevel.SEDENTARY);

        PatientResponseDTO response = patientService.create(request);

        assertThat(response.getAge()).isEqualTo(30);
        assertThat(response.getBmi()).isEqualByComparingTo("24.69");
        assertThat(response.getBmiClassification()).isEqualTo("bmi.classification.normal");
        // Mifflin-St Jeor: 10*80 + 6.25*180 - 5*30 + 5 = 1780
        assertThat(response.getBmr()).isEqualByComparingTo("1780.00");
        // TDEE = BMR * fator sedentário (1.2)
        assertThat(response.getDailyCalorieExpenditure()).isEqualByComparingTo("2136.00");
    }

    @Test
    void shouldUseManualDailyCaloriesInsteadOfCalculatedWhenProvided() {
        PatientCreateRequestDTO request = validCreateRequest();
        request.setManualDailyCalories(new BigDecimal("3000"));

        PatientResponseDTO response = patientService.create(request);

        assertThat(response.getDailyCalorieExpenditure()).isEqualByComparingTo("3000");
    }

    private NutritionProfile existingProfile(Long patientId, String name, Gender gender, Goal goal, ActivityLevel activityLevel) {
        PatientCore patientCore = PatientCore.builder()
                .id(patientId)
                .name(name)
                .birthDate(LocalDate.of(1985, 1, 1))
                .gender(gender)
                .build();
        return NutritionProfile.builder()
                .id(50L)
                .patientCore(patientCore)
                .height(new BigDecimal("1.80"))
                .weight(new BigDecimal("90.00"))
                .goal(goal)
                .activityLevel(activityLevel)
                .build();
    }

    @Test
    void shouldUpdateExistingPatient() {
        NutritionProfile existing = existingProfile(1L, "Old Name", Gender.MALE, Goal.MAINTAIN_WEIGHT, ActivityLevel.SEDENTARY);
        when(nutritionProfileRepository.findByPatientCoreId(1L)).thenReturn(Optional.of(existing));

        PatientUpdateRequestDTO request = new PatientUpdateRequestDTO();
        request.setName("New Name");
        request.setBirthDate(LocalDate.of(1985, 1, 1));
        request.setGender(Gender.MALE);
        request.setHeight(new BigDecimal("1.80"));
        request.setWeight(new BigDecimal("88.00"));
        request.setGoal(Goal.LOSE_WEIGHT);
        request.setActivityLevel(ActivityLevel.LIGHTLY_ACTIVE);

        PatientResponseDTO response = patientService.update(1L, request);

        assertThat(response.getName()).isEqualTo("New Name");
        assertThat(response.getWeight()).isEqualByComparingTo("88.00");
        assertThat(response.getGoal()).isEqualTo(Goal.LOSE_WEIGHT);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentPatient() {
        when(nutritionProfileRepository.findByPatientCoreId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.update(99L, validUpdateRequest()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void shouldThrowWhenFindingNonExistentPatient() {
        when(nutritionProfileRepository.findByPatientCoreId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.findById(99L))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentPatient() {
        when(nutritionProfileRepository.findByPatientCoreId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.delete(99L))
                .isInstanceOf(CustomException.class);

        verify(nutritionProfileRepository, never()).delete(any());
    }

    @Test
    void shouldDeleteOnlyNutritionProfileAndPreservePatientCore() {
        NutritionProfile existing = existingProfile(1L, "Maria Souza", Gender.FEMALE, Goal.MAINTAIN_WEIGHT, ActivityLevel.SEDENTARY);
        when(nutritionProfileRepository.findByPatientCoreId(1L)).thenReturn(Optional.of(existing));

        patientService.delete(1L);

        verify(nutritionProfileRepository).delete(existing);
        verify(patientCoreRepository, never()).delete(any());
        verify(patientCoreRepository, never()).deleteById(any());
    }

    private PatientUpdateRequestDTO validUpdateRequest() {
        PatientUpdateRequestDTO request = new PatientUpdateRequestDTO();
        request.setName("Someone");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setGender(Gender.MALE);
        request.setHeight(new BigDecimal("1.75"));
        request.setWeight(new BigDecimal("75.00"));
        request.setGoal(Goal.MAINTAIN_WEIGHT);
        request.setActivityLevel(ActivityLevel.SEDENTARY);
        return request;
    }
}
