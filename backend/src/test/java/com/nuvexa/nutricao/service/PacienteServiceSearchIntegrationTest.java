package com.nuvexa.nutricao.service;

import com.nuvexa.core.service.ContextoDeAutenticacao;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.repository.OrganizacaoRepository;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.platform.config.MessageConfig;
import com.nuvexa.platform.config.ModelMapperConfig;
import com.nuvexa.platform.config.QuerydslConfig;
import com.nuvexa.nutricao.calculator.ImcCalculator;
import com.nuvexa.nutricao.calculator.GastoCaloricoCalculator;
import com.nuvexa.nutricao.calculator.TaxaMetabolicaCalculator;
import com.nuvexa.nutricao.dto.response.PacienteResponseDTO;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.nutricao.model.PerfilNutricional;
import com.nuvexa.nutricao.repository.PerfilNutricionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QuerydslConfig.class, MessageConfig.class, ModelMapperConfig.class,
        ImcCalculator.class, TaxaMetabolicaCalculator.class, GastoCaloricoCalculator.class, PacienteService.class})
class PacienteServiceSearchIntegrationTest {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PerfilNutricionalRepository perfilNutricionalRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @MockitoBean
    private ContextoDeAutenticacao contextoDeAutenticacao;

    private Organizacao organizacao;

    @BeforeEach
    void setUp() {
        organizacao = organizacaoRepository.saveAndFlush(Organizacao.builder()
                .nome("Clínica de Teste")
                .tipo(TipoOrganizacao.CLINICA)
                .status(StatusOrganizacao.ATIVA)
                .build());
        when(contextoDeAutenticacao.organizacaoAtual()).thenReturn(organizacao);
        when(contextoDeAutenticacao.organizacaoAtualId()).thenReturn(organizacao.getId());
    }

    private void novoPacienteRequest(String name) {
        Paciente paciente = pacienteRepository.saveAndFlush(Paciente.builder()
                .organizacao(organizacao)
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
                .extracting(PacienteResponseDTO::getNome)
                .containsExactlyInAnyOrder("Joao Pereira", "Ana Pereira");
    }

    @Test
    void shouldReturnEveryoneWhenSearchIsBlank() {
        novoPacienteRequest("Joao Pereira");
        novoPacienteRequest("Carlos Lima");

        List<PacienteResponseDTO> results = pacienteService.findAll("   ");

        assertThat(results)
                .extracting(PacienteResponseDTO::getNome)
                .contains("Joao Pereira", "Carlos Lima");
    }
}
