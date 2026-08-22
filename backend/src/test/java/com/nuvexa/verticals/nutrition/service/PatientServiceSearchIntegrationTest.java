package com.nuvexa.verticals.nutrition.service;

import com.nuvexa.core.patient.model.Gender;
import com.nuvexa.core.patient.model.PatientCore;
import com.nuvexa.core.patient.repository.PatientCoreRepository;
import com.nuvexa.platform.config.MessageConfig;
import com.nuvexa.platform.config.querydsl.QuerydslConfig;
import com.nuvexa.verticals.nutrition.calculator.BmiCalculator;
import com.nuvexa.verticals.nutrition.calculator.CalorieExpenditureCalculator;
import com.nuvexa.verticals.nutrition.calculator.MetabolicCalculator;
import com.nuvexa.verticals.nutrition.dto.response.PatientResponseDTO;
import com.nuvexa.verticals.nutrition.mapper.PatientMapper;
import com.nuvexa.verticals.nutrition.model.ActivityLevel;
import com.nuvexa.verticals.nutrition.model.Goal;
import com.nuvexa.verticals.nutrition.model.NutritionProfile;
import com.nuvexa.verticals.nutrition.repository.NutritionProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QuerydslConfig.class, MessageConfig.class, PatientMapper.class,
        BmiCalculator.class, MetabolicCalculator.class, CalorieExpenditureCalculator.class, PatientService.class})
class PatientServiceSearchIntegrationTest {

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientCoreRepository patientCoreRepository;

    @Autowired
    private NutritionProfileRepository nutritionProfileRepository;

    private void newPatient(String name) {
        PatientCore patientCore = patientCoreRepository.saveAndFlush(PatientCore.builder()
                .name(name)
                .birthDate(LocalDate.of(1990, 5, 20))
                .gender(Gender.FEMALE)
                .build());

        nutritionProfileRepository.saveAndFlush(NutritionProfile.builder()
                .patientCore(patientCore)
                .height(new BigDecimal("1.65"))
                .weight(new BigDecimal("62.50"))
                .goal(Goal.LOSE_WEIGHT)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .build());
    }

    @Test
    void shouldFindPatientsByNameCaseInsensitive() {
        newPatient("Joao Pereira");
        newPatient("Ana Pereira");
        newPatient("Carlos Lima");

        List<PatientResponseDTO> results = patientService.findAll("pereira");

        assertThat(results).hasSize(2)
                .extracting(PatientResponseDTO::getName)
                .containsExactlyInAnyOrder("Joao Pereira", "Ana Pereira");
    }

    @Test
    void shouldReturnEveryoneWhenSearchIsBlank() {
        newPatient("Joao Pereira");
        newPatient("Carlos Lima");

        List<PatientResponseDTO> results = patientService.findAll("   ");

        assertThat(results)
                .extracting(PatientResponseDTO::getName)
                .contains("Joao Pereira", "Carlos Lima");
    }
}
