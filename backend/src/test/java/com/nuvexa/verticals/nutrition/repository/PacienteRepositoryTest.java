package com.nuvexa.verticais.nutricao.repository;

import com.nuvexa.nucleo.paciente.model.Sexo;
import com.nuvexa.nucleo.paciente.model.Paciente;
import com.nuvexa.nucleo.paciente.repository.PacienteRepository;
import com.nuvexa.verticais.nutricao.model.NivelAtividade;
import com.nuvexa.verticais.nutricao.model.Objetivo;
import com.nuvexa.verticais.nutricao.model.PerfilNutricional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PacienteRepositoryTest {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PerfilNutricionalRepository perfilNutricionalRepository;

    private Paciente novoPaciente(String name) {
        return Paciente.builder()
                .name(name)
                .birthDate(LocalDate.of(1990, 5, 20))
                .gender(Sexo.FEMALE)
                .build();
    }

    private PerfilNutricional novoPerfilNutricional(Paciente paciente) {
        return PerfilNutricional.builder()
                .paciente(paciente)
                .height(new BigDecimal("1.65"))
                .weight(new BigDecimal("62.50"))
                .goal(Objetivo.LOSE_WEIGHT)
                .activityLevel(NivelAtividade.MODERATELY_ACTIVE)
                .build();
    }

    @Test
    void shouldPersistAndReadBackAllFields() {
        Paciente paciente = pacienteRepository.saveAndFlush(novoPaciente("Maria Souza"));
        PerfilNutricional saved = perfilNutricionalRepository.saveAndFlush(novoPerfilNutricional(paciente));

        PerfilNutricional found = perfilNutricionalRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getPaciente().getId()).isEqualTo(paciente.getId());
        assertThat(found.getPaciente().getName()).isEqualTo("Maria Souza");
        assertThat(found.getPaciente().getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 20));
        assertThat(found.getPaciente().getGender()).isEqualTo(Sexo.FEMALE);
        assertThat(found.getHeight()).isEqualByComparingTo("1.65");
        assertThat(found.getWeight()).isEqualByComparingTo("62.50");
        assertThat(found.getGoal()).isEqualTo(Objetivo.LOSE_WEIGHT);
        assertThat(found.getActivityLevel()).isEqualTo(NivelAtividade.MODERATELY_ACTIVE);
        assertThat(found.getManualDailyCalories()).isNull();
        assertThat(found.getNotes()).isNull();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateUpdatedAtOnChange() {
        Paciente paciente = pacienteRepository.saveAndFlush(novoPaciente("Pedro Alves"));
        PerfilNutricional saved = perfilNutricionalRepository.saveAndFlush(novoPerfilNutricional(paciente));
        var firstUpdatedAt = saved.getUpdatedAt();

        saved.setWeight(new BigDecimal("70.00"));
        PerfilNutricional updated = perfilNutricionalRepository.saveAndFlush(saved);

        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(firstUpdatedAt);
        assertThat(updated.getCreatedAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    void deletingNutritionProfileShouldPreservePatientCore() {
        Paciente paciente = pacienteRepository.saveAndFlush(novoPaciente("Joana Lima"));
        PerfilNutricional saved = perfilNutricionalRepository.saveAndFlush(novoPerfilNutricional(paciente));

        perfilNutricionalRepository.delete(saved);
        perfilNutricionalRepository.flush();

        assertThat(perfilNutricionalRepository.findById(saved.getId())).isEmpty();
        assertThat(pacienteRepository.findById(paciente.getId())).isPresent();
    }
}
