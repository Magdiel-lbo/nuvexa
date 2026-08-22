package com.nuvexa.verticais.nutricao.service;

import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.core.paciente.model.Paciente;
import com.nuvexa.core.paciente.repository.PacienteRepository;
import com.nuvexa.plataforma.config.MessageConfig;
import com.nuvexa.plataforma.config.querydsl.QuerydslConfig;
import com.nuvexa.verticais.nutricao.calculadora.ImcCalculator;
import com.nuvexa.verticais.nutricao.calculadora.GastoCaloricoCalculator;
import com.nuvexa.verticais.nutricao.calculadora.TaxaMetabolicaCalculator;
import com.nuvexa.verticais.nutricao.dto.response.PacienteResponseDTO;
import com.nuvexa.verticais.nutricao.mapper.PacienteMapper;
import com.nuvexa.verticais.nutricao.model.NivelAtividade;
import com.nuvexa.verticais.nutricao.model.Objetivo;
import com.nuvexa.verticais.nutricao.model.PerfilNutricional;
import com.nuvexa.verticais.nutricao.repository.PerfilNutricionalRepository;
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
@Import({QuerydslConfig.class, MessageConfig.class, PacienteMapper.class,
        ImcCalculator.class, TaxaMetabolicaCalculator.class, GastoCaloricoCalculator.class, PacienteService.class})
class PacienteServiceSearchIntegrationTest {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PerfilNutricionalRepository perfilNutricionalRepository;

    private void novoPacienteRequest(String name) {
        Paciente paciente = pacienteRepository.saveAndFlush(Paciente.builder()
                .nome(name)
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build());

        perfilNutricionalRepository.saveAndFlush(PerfilNutricional.builder()
                .paciente(paciente)
                .altura(new BigDecimal("1.65"))
                .peso(new BigDecimal("62.50"))
                .objetivo(Objetivo.EMAGRECIMENTO)
                .nivelAtividade(NivelAtividade.MODERADAMENTE_ATIVO)
                .build());
    }

    @Test
    void shouldFindPatientsByNameCaseInsensitive() {
        novoPacienteRequest("Joao Pereira");
        novoPacienteRequest("Ana Pereira");
        novoPacienteRequest("Carlos Lima");

        List<PacienteResponseDTO> results = pacienteService.findAll("pereira");

        assertThat(results).hasSize(2)
                .extracting(PacienteResponseDTO::getName)
                .containsExactlyInAnyOrder("Joao Pereira", "Ana Pereira");
    }

    @Test
    void shouldReturnEveryoneWhenSearchIsBlank() {
        novoPacienteRequest("Joao Pereira");
        novoPacienteRequest("Carlos Lima");

        List<PacienteResponseDTO> results = pacienteService.findAll("   ");

        assertThat(results)
                .extracting(PacienteResponseDTO::getName)
                .contains("Joao Pereira", "Carlos Lima");
    }
}
