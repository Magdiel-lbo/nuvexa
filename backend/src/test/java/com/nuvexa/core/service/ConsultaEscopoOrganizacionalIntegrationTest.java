package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.ConsultaCreateRequestDTO;
import com.nuvexa.core.dto.request.ConsultaUpdateRequestDTO;
import com.nuvexa.core.dto.response.ConsultaResponseDTO;
import com.nuvexa.core.model.Consulta;
import com.nuvexa.core.model.StatusConsulta;
import com.nuvexa.core.model.TipoConsulta;
import com.nuvexa.core.repository.ConsultaRepository;
import com.nuvexa.core.service.ContextoDeAutenticacao;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.repository.OrganizacaoRepository;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Sexo;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Mesmo isolamento multi-organização já provado em PacienteEscopoOrganizacionalIntegrationTest,
 * agora para Consulta.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QuerydslConfig.class, MessageConfig.class, ModelMapperConfig.class, ConsultaService.class})
class ConsultaEscopoOrganizacionalIntegrationTest {

    @Autowired
    private ConsultaService consultaService;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @MockitoBean
    private ContextoDeAutenticacao contextoDeAutenticacao;

    private Organizacao minhaOrganizacao;
    private Organizacao outraOrganizacao;
    private Paciente meuPaciente;
    private Paciente pacienteAlheio;

    @BeforeEach
    void setUp() {
        minhaOrganizacao = novaOrganizacao("Clínica A");
        outraOrganizacao = novaOrganizacao("Clínica B");
        meuPaciente = novoPaciente(minhaOrganizacao, "Ana da Minha Clinica");
        pacienteAlheio = novoPaciente(outraOrganizacao, "Bruno da Outra Clinica");
        estarLogadoEm(minhaOrganizacao);
    }

    private Organizacao novaOrganizacao(String nome) {
        return organizacaoRepository.saveAndFlush(Organizacao.builder()
                .nome(nome)
                .tipo(TipoOrganizacao.CLINICA)
                .status(StatusOrganizacao.ATIVA)
                .build());
    }

    private Paciente novoPaciente(Organizacao organizacao, String nome) {
        return pacienteRepository.saveAndFlush(Paciente.builder()
                .organizacao(organizacao)
                .nome(nome)
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build());
    }

    private void estarLogadoEm(Organizacao organizacao) {
        when(contextoDeAutenticacao.organizacaoAtual()).thenReturn(organizacao);
        when(contextoDeAutenticacao.organizacaoAtualId()).thenReturn(organizacao.getId());
    }

    private Consulta novaConsulta(Organizacao organizacao, Paciente paciente) {
        return consultaRepository.saveAndFlush(Consulta.builder()
                .organizacao(organizacao)
                .paciente(paciente)
                .dataHora(LocalDateTime.of(2026, 10, 9, 14, 30))
                .duracaoMinutos(30)
                .tipo(TipoConsulta.RETORNO)
                .status(StatusConsulta.AGENDADA)
                .build());
    }

    private ConsultaCreateRequestDTO createRequest(Long pacienteId) {
        ConsultaCreateRequestDTO request = new ConsultaCreateRequestDTO();
        request.setPacienteId(pacienteId);
        request.setDataHora(LocalDateTime.of(2026, 10, 9, 14, 30));
        request.setDuracaoMinutos(30);
        request.setTipo(TipoConsulta.PRIMEIRA_CONSULTA);
        request.setStatus(StatusConsulta.AGENDADA);
        return request;
    }

    private ConsultaUpdateRequestDTO updateRequest() {
        ConsultaUpdateRequestDTO request = new ConsultaUpdateRequestDTO();
        request.setDataHora(LocalDateTime.of(2026, 10, 10, 9, 0));
        request.setDuracaoMinutos(45);
        request.setTipo(TipoConsulta.AVALIACAO);
        request.setStatus(StatusConsulta.CONFIRMADA);
        return request;
    }

    @Test
    void deveListarApenasConsultasDaOrganizacaoAtual() {
        novaConsulta(minhaOrganizacao, meuPaciente);
        novaConsulta(outraOrganizacao, pacienteAlheio);

        List<ConsultaResponseDTO> resultado = consultaService.findAll(null, null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getPacienteNome()).isEqualTo("Ana da Minha Clinica");
    }

    @Test
    void deveCriarConsultaParaPacienteDaPropriaOrganizacao() {
        ConsultaResponseDTO criada = consultaService.create(createRequest(meuPaciente.getId()));

        Consulta persistida = consultaRepository.findById(criada.getId()).orElseThrow();
        assertThat(persistida.getOrganizacao().getId()).isEqualTo(minhaOrganizacao.getId());
    }

    @Test
    void naoDeveCriarConsultaParaPacienteDeOutraOrganizacao() {
        assertThatThrownBy(() -> consultaService.create(createRequest(pacienteAlheio.getId())))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deveEditarConsultaDaPropriaOrganizacao() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente);

        ConsultaResponseDTO atualizada = consultaService.update(consulta.getId(), updateRequest());

        assertThat(atualizada.getStatus()).isEqualTo(StatusConsulta.CONFIRMADA);
        assertThat(atualizada.getTipo()).isEqualTo(TipoConsulta.AVALIACAO);
    }

    @Test
    void naoDeveAcessarConsultaDeOutraOrganizacao() {
        Consulta alheia = novaConsulta(outraOrganizacao, pacienteAlheio);

        assertThatThrownBy(() -> consultaService.findById(alheia.getId()))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void naoDeveEditarConsultaDeOutraOrganizacao() {
        Consulta alheia = novaConsulta(outraOrganizacao, pacienteAlheio);

        assertThatThrownBy(() -> consultaService.update(alheia.getId(), updateRequest()))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        assertThat(consultaRepository.findById(alheia.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusConsulta.AGENDADA);
    }

    @Test
    void naoDeveExcluirConsultaDeOutraOrganizacao() {
        Consulta alheia = novaConsulta(outraOrganizacao, pacienteAlheio);

        assertThatThrownBy(() -> consultaService.delete(alheia.getId()))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        assertThat(consultaRepository.findById(alheia.getId())).isPresent();
    }

    @Test
    void deveFiltrarPorPaciente() {
        novaConsulta(minhaOrganizacao, meuPaciente);
        Paciente outroPacienteMesmaOrg = novoPaciente(minhaOrganizacao, "Carla da Minha Clinica");
        novaConsulta(minhaOrganizacao, outroPacienteMesmaOrg);

        List<ConsultaResponseDTO> resultado = consultaService.findAll(meuPaciente.getId(), null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getPacienteId()).isEqualTo(meuPaciente.getId());
    }

    @Test
    void deveFiltrarPorNomeDoPacienteCaseInsensitive() {
        novaConsulta(minhaOrganizacao, meuPaciente);
        Paciente carla = novoPaciente(minhaOrganizacao, "Carla da Minha Clinica");
        novaConsulta(minhaOrganizacao, carla);

        List<ConsultaResponseDTO> resultado = consultaService.findAll(null, "ana");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getPacienteNome()).isEqualTo("Ana da Minha Clinica");
    }

    @Test
    void deveIgnorarBuscaEmBranco() {
        novaConsulta(minhaOrganizacao, meuPaciente);

        List<ConsultaResponseDTO> resultado = consultaService.findAll(null, "   ");

        assertThat(resultado).hasSize(1);
    }
}
