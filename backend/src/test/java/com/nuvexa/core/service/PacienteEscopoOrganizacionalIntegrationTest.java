package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.core.dto.request.PacienteUpdateRequestDTO;
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
import com.nuvexa.platform.exception.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Isolamento multi-organização de Paciente (core) — só dado genérico. O isolamento do relatório
 * de Pacientes (que depende de PerfilNutricional/Avaliacao) fica em
 * PacienteRelatorioServiceEscopoOrganizacionalIntegrationTest, na vertical nutricao.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QuerydslConfig.class, MessageConfig.class, ModelMapperConfig.class, OrganizacaoScopedContext.class, PacienteService.class})
class PacienteEscopoOrganizacionalIntegrationTest {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @MockitoBean
    private ContextoDeAutenticacao contextoDeAutenticacao;

    private Organizacao minhaOrganizacao;
    private Organizacao outraOrganizacao;

    @BeforeEach
    void setUp() {
        minhaOrganizacao = novaOrganizacao("Clínica A");
        outraOrganizacao = novaOrganizacao("Clínica B");
        estarLogadoEm(minhaOrganizacao);
    }

    private Organizacao novaOrganizacao(String nome) {
        return organizacaoRepository.saveAndFlush(Organizacao.builder()
                .nome(nome)
                .tipo(TipoOrganizacao.CLINICA)
                .status(StatusOrganizacao.ATIVA)
                .build());
    }

    private void estarLogadoEm(Organizacao organizacao) {
        when(contextoDeAutenticacao.organizacaoAtual()).thenReturn(organizacao);
        when(contextoDeAutenticacao.organizacaoAtualId()).thenReturn(organizacao.getId());
    }

    private Paciente novoPaciente(Organizacao organizacao, String nome) {
        return pacienteRepository.saveAndFlush(Paciente.builder()
                .organizacao(organizacao)
                .nome(nome)
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build());
    }

    private PacienteCreateRequestDTO createRequest(String nome) {
        return PacienteCreateRequestDTO.builder()
                .nome(nome)
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build();
    }

    private PacienteUpdateRequestDTO updateRequest(String nome) {
        return PacienteUpdateRequestDTO.builder()
                .nome(nome)
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build();
    }

    @Test
    void deveListarApenasPacientesDaOrganizacaoAtual() {
        novoPaciente(minhaOrganizacao, "Ana da Minha Clinica");
        novoPaciente(outraOrganizacao, "Bruno da Outra Clinica");

        List<PacienteResponseDTO> resultado = pacienteService.findAll(null);

        assertThat(resultado)
                .extracting(PacienteResponseDTO::getNome)
                .containsExactly("Ana da Minha Clinica")
                .doesNotContain("Bruno da Outra Clinica");
    }

    @Test
    void deveCriarPacienteNaOrganizacaoAtual() {
        PacienteResponseDTO criado = pacienteService.create(createRequest("Carla Nova"));

        Paciente persistido = pacienteRepository.findById(criado.getId()).orElseThrow();
        assertThat(persistido.getOrganizacao().getId()).isEqualTo(minhaOrganizacao.getId());
    }

    @Test
    void deveEditarPacienteDaPropriaOrganizacaoPreservandoEscopo() {
        Paciente paciente = novoPaciente(minhaOrganizacao, "Diana Original");

        PacienteResponseDTO atualizado = pacienteService.update(paciente.getId(), updateRequest("Diana Editada"));

        assertThat(atualizado.getNome()).isEqualTo("Diana Editada");
        Paciente persistido = pacienteRepository.findById(paciente.getId()).orElseThrow();
        assertThat(persistido.getOrganizacao().getId()).isEqualTo(minhaOrganizacao.getId());
    }

    @Test
    void naoDeveAcessarPacienteDeOutraOrganizacao() {
        Paciente alheio = novoPaciente(outraOrganizacao, "Eduardo Alheio");

        assertThatThrownBy(() -> pacienteService.findById(alheio.getId()))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void naoDeveAlterarPacienteDeOutraOrganizacao() {
        Paciente alheio = novoPaciente(outraOrganizacao, "Fabio Alheio");

        assertThatThrownBy(() -> pacienteService.update(alheio.getId(), updateRequest("Invadido")))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        assertThat(pacienteRepository.findById(alheio.getId()).orElseThrow().getNome())
                .isEqualTo("Fabio Alheio");
    }

    @Test
    void usuarioSemVinculoNaoDeveAcessarDados() {
        novoPaciente(minhaOrganizacao, "Joana Bloqueada");
        when(contextoDeAutenticacao.organizacaoAtualId())
                .thenThrow(new NegocioException(HttpStatus.FORBIDDEN, "contexto.semVinculo"));

        assertThatThrownBy(() -> pacienteService.findAll(null))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void mesmaBaseDeveResponderDiferentePorOrganizacao() {
        novoPaciente(minhaOrganizacao, "Katia A");
        novoPaciente(outraOrganizacao, "Lucas B");

        assertThat(pacienteService.findAll(null)).extracting(PacienteResponseDTO::getNome).containsExactly("Katia A");

        estarLogadoEm(outraOrganizacao);
        assertThat(pacienteService.findAll(null)).extracting(PacienteResponseDTO::getNome).containsExactly("Lucas B");
    }
}
