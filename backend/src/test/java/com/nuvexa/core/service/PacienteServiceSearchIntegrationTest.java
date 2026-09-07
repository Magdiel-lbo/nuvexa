package com.nuvexa.core.service;

import com.nuvexa.core.dto.response.PacienteResponseDTO;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.repository.OrganizacaoRepository;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.platform.config.MessageConfig;
import com.nuvexa.platform.config.ModelMapperConfig;
import com.nuvexa.platform.config.QuerydslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QuerydslConfig.class, MessageConfig.class, ModelMapperConfig.class, OrganizacaoScopedContext.class, PacienteService.class})
class PacienteServiceSearchIntegrationTest {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private PacienteRepository pacienteRepository;

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

    private void novoPaciente(String nome) {
        pacienteRepository.saveAndFlush(Paciente.builder()
                .organizacao(organizacao)
                .nome(nome)
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build());
    }

    @Test
    void deveBuscarPacientesPorNomeCaseInsensitive() {
        novoPaciente("Joao Pereira");
        novoPaciente("Ana Pereira");
        novoPaciente("Carlos Lima");

        List<PacienteResponseDTO> resultado = pacienteService.findAll("pereira");

        assertThat(resultado).hasSize(2)
                .extracting(PacienteResponseDTO::getNome)
                .containsExactlyInAnyOrder("Joao Pereira", "Ana Pereira");
    }

    @Test
    void deveRetornarTodosQuandoBuscaEmBranco() {
        novoPaciente("Joao Pereira");
        novoPaciente("Carlos Lima");

        List<PacienteResponseDTO> resultado = pacienteService.findAll("   ");

        assertThat(resultado)
                .extracting(PacienteResponseDTO::getNome)
                .contains("Joao Pereira", "Carlos Lima");
    }
}
