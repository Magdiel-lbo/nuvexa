package com.nuvexa.nutricao.repository;

import com.nuvexa.core.organizacao.model.Organizacao;
import com.nuvexa.core.organizacao.model.StatusOrganizacao;
import com.nuvexa.core.organizacao.model.TipoOrganizacao;
import com.nuvexa.core.organizacao.repository.OrganizacaoRepository;
import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.core.paciente.model.Paciente;
import com.nuvexa.core.paciente.repository.PacienteRepository;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.nutricao.model.PerfilNutricional;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    private Organizacao organizacao;

    @BeforeEach
    void setUp() {
        organizacao = organizacaoRepository.saveAndFlush(Organizacao.builder()
                .nome("Clínica de Teste")
                .tipo(TipoOrganizacao.CLINICA)
                .status(StatusOrganizacao.ATIVA)
                .build());
    }

    private Paciente novoPaciente(String name) {
        return Paciente.builder()
                .organizacao(organizacao)
                .nome(name)
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build();
    }

    private PerfilNutricional novoPerfilNutricional(Paciente paciente) {
        return PerfilNutricional.builder()
                .paciente(paciente)
                .altura(new BigDecimal("1.65"))
                .peso(new BigDecimal("62.50"))
                .objetivo(Objetivo.EMAGRECIMENTO)
                .nivelAtividade(NivelAtividade.MODERADAMENTE_ATIVO)
                .build();
    }

    @Test
    void shouldPersistAndReadBackAllFields() {
        Paciente paciente = pacienteRepository.saveAndFlush(novoPaciente("Maria Souza"));
        PerfilNutricional saved = perfilNutricionalRepository.saveAndFlush(novoPerfilNutricional(paciente));

        PerfilNutricional found = perfilNutricionalRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getPaciente().getId()).isEqualTo(paciente.getId());
        assertThat(found.getPaciente().getNome()).isEqualTo("Maria Souza");
        assertThat(found.getPaciente().getDataNascimento()).isEqualTo(LocalDate.of(1990, 5, 20));
        assertThat(found.getPaciente().getSexo()).isEqualTo(Sexo.FEMININO);
        assertThat(found.getAltura()).isEqualByComparingTo("1.65");
        assertThat(found.getPeso()).isEqualByComparingTo("62.50");
        assertThat(found.getObjetivo()).isEqualTo(Objetivo.EMAGRECIMENTO);
        assertThat(found.getNivelAtividade()).isEqualTo(NivelAtividade.MODERADAMENTE_ATIVO);
        assertThat(found.getCaloriasDiariasManuais()).isNull();
        assertThat(found.getObservacoes()).isNull();
        assertThat(found.getCriadoEm()).isNotNull();
        assertThat(found.getAtualizadoEm()).isNotNull();
    }

    @Test
    void shouldUpdateUpdatedAtOnChange() {
        Paciente paciente = pacienteRepository.saveAndFlush(novoPaciente("Pedro Alves"));
        PerfilNutricional saved = perfilNutricionalRepository.saveAndFlush(novoPerfilNutricional(paciente));
        var firstUpdatedAt = saved.getAtualizadoEm();

        saved.setPeso(new BigDecimal("70.00"));
        PerfilNutricional updated = perfilNutricionalRepository.saveAndFlush(saved);

        assertThat(updated.getAtualizadoEm()).isAfterOrEqualTo(firstUpdatedAt);
        assertThat(updated.getCriadoEm()).isEqualTo(saved.getCriadoEm());
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
