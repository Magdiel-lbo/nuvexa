package com.nuvexa.verticals.nutrition.repository;

import com.nuvexa.core.patient.model.Gender;
import com.nuvexa.core.patient.model.PatientCore;
import com.nuvexa.core.patient.repository.PatientCoreRepository;
import com.nuvexa.verticals.nutrition.model.ActivityLevel;
import com.nuvexa.verticals.nutrition.model.Goal;
import com.nuvexa.verticals.nutrition.model.NutritionProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PatientRepositoryTest {

    @Autowired
    private PatientCoreRepository patientCoreRepository;

    @Autowired
    private NutritionProfileRepository nutritionProfileRepository;

    private PatientCore newPatientCore(String name) {
        return PatientCore.builder()
                .name(name)
                .birthDate(LocalDate.of(1990, 5, 20))
                .gender(Gender.FEMALE)
                .build();
    }

    private NutritionProfile newNutritionProfile(PatientCore patientCore) {
        return NutritionProfile.builder()
                .patientCore(patientCore)
                .height(new BigDecimal("1.65"))
                .weight(new BigDecimal("62.50"))
                .goal(Goal.LOSE_WEIGHT)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .build();
    }

    @Test
    void shouldPersistAndReadBackAllFields() {
        PatientCore patientCore = patientCoreRepository.saveAndFlush(newPatientCore("Maria Souza"));
        NutritionProfile saved = nutritionProfileRepository.saveAndFlush(newNutritionProfile(patientCore));

        NutritionProfile found = nutritionProfileRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getPatientCore().getId()).isEqualTo(patientCore.getId());
        assertThat(found.getPatientCore().getName()).isEqualTo("Maria Souza");
        assertThat(found.getPatientCore().getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 20));
        assertThat(found.getPatientCore().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(found.getHeight()).isEqualByComparingTo("1.65");
        assertThat(found.getWeight()).isEqualByComparingTo("62.50");
        assertThat(found.getGoal()).isEqualTo(Goal.LOSE_WEIGHT);
        assertThat(found.getActivityLevel()).isEqualTo(ActivityLevel.MODERATELY_ACTIVE);
        assertThat(found.getManualDailyCalories()).isNull();
        assertThat(found.getNotes()).isNull();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateUpdatedAtOnChange() {
        PatientCore patientCore = patientCoreRepository.saveAndFlush(newPatientCore("Pedro Alves"));
        NutritionProfile saved = nutritionProfileRepository.saveAndFlush(newNutritionProfile(patientCore));
        var firstUpdatedAt = saved.getUpdatedAt();

        saved.setWeight(new BigDecimal("70.00"));
        NutritionProfile updated = nutritionProfileRepository.saveAndFlush(saved);

        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(firstUpdatedAt);
        assertThat(updated.getCreatedAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    void deletingNutritionProfileShouldPreservePatientCore() {
        PatientCore patientCore = patientCoreRepository.saveAndFlush(newPatientCore("Joana Lima"));
        NutritionProfile saved = nutritionProfileRepository.saveAndFlush(newNutritionProfile(patientCore));

        nutritionProfileRepository.delete(saved);
        nutritionProfileRepository.flush();

        assertThat(nutritionProfileRepository.findById(saved.getId())).isEmpty();
        assertThat(patientCoreRepository.findById(patientCore.getId())).isPresent();
    }
}
