package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.ConsultaCreateRequestDTO;
import com.nuvexa.core.dto.request.ConsultaUpdateRequestDTO;
import com.nuvexa.core.dto.response.ConsultaResponseDTO;
import com.nuvexa.core.dto.response.ProfissionalResponseDTO;
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
import com.nuvexa.core.model.PapelOrganizacional;
import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.core.repository.UsuarioRepository;
import com.nuvexa.core.repository.VinculoRepository;
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
@Import({QuerydslConfig.class, MessageConfig.class, ModelMapperConfig.class, OrganizacaoScopedContext.class, ConsultaService.class})
class ConsultaEscopoOrganizacionalIntegrationTest {

    @Autowired
    private ConsultaService consultaService;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VinculoRepository vinculoRepository;

    @MockitoBean
    private ContextoDeAutenticacao contextoDeAutenticacao;

    private Organizacao minhaOrganizacao;
    private Organizacao outraOrganizacao;
    private Paciente meuPaciente;
    private Paciente pacienteAlheio;
    private Usuario meuProfissional;
    private Usuario profissionalAlheio;

    @BeforeEach
    void setUp() {
        minhaOrganizacao = novaOrganizacao("Clínica A");
        outraOrganizacao = novaOrganizacao("Clínica B");
        meuPaciente = novoPaciente(minhaOrganizacao, "Ana da Minha Clinica");
        pacienteAlheio = novoPaciente(outraOrganizacao, "Bruno da Outra Clinica");
        meuProfissional = novoProfissionalVinculado(minhaOrganizacao, "Joana Nutri");
        profissionalAlheio = novoProfissionalVinculado(outraOrganizacao, "Carlos Nutri");
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

    private Usuario novoProfissionalVinculado(Organizacao organizacao, String nome) {
        Usuario usuario = usuarioRepository.saveAndFlush(Usuario.builder()
                .nome(nome)
                .email(nome.toLowerCase().replace(" ", ".") + "@nuvexa.com")
                .senha("hash")
                .perfil(Perfil.PROFISSIONAL)
                .ativo(true)
                .build());
        vinculoRepository.saveAndFlush(Vinculo.builder()
                .usuario(usuario)
                .organizacao(organizacao)
                .papel(PapelOrganizacional.MEMBRO)
                .ativo(true)
                .build());
        return usuario;
    }

    private Consulta novaConsulta(Organizacao organizacao, Paciente paciente, Usuario profissional) {
        return consultaRepository.saveAndFlush(Consulta.builder()
                .organizacao(organizacao)
                .paciente(paciente)
                .profissional(profissional)
                .dataHora(LocalDateTime.of(2026, 10, 9, 14, 30))
                .duracaoMinutos(30)
                .tipo(TipoConsulta.RETORNO)
                .status(StatusConsulta.AGENDADA)
                .build());
    }

    private ConsultaCreateRequestDTO createRequest(Long pacienteId, Long profissionalId) {
        ConsultaCreateRequestDTO request = new ConsultaCreateRequestDTO();
        request.setPacienteId(pacienteId);
        request.setProfissionalId(profissionalId);
        request.setDataHora(LocalDateTime.of(2026, 10, 9, 14, 30));
        request.setDuracaoMinutos(30);
        request.setTipo(TipoConsulta.PRIMEIRA_CONSULTA);
        request.setStatus(StatusConsulta.AGENDADA);
        return request;
    }

    private ConsultaUpdateRequestDTO updateRequest(Long profissionalId) {
        ConsultaUpdateRequestDTO request = new ConsultaUpdateRequestDTO();
        request.setProfissionalId(profissionalId);
        request.setDataHora(LocalDateTime.of(2026, 10, 10, 9, 0));
        request.setDuracaoMinutos(45);
        request.setTipo(TipoConsulta.AVALIACAO);
        request.setStatus(StatusConsulta.CONFIRMADA);
        return request;
    }

    @Test
    void deveListarApenasConsultasDaOrganizacaoAtual() {
        novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional);
        novaConsulta(outraOrganizacao, pacienteAlheio, profissionalAlheio);

        List<ConsultaResponseDTO> resultado = consultaService.findAll(null, null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getPacienteNome()).isEqualTo("Ana da Minha Clinica");
    }

    @Test
    void deveCriarConsultaParaPacienteDaPropriaOrganizacao() {
        ConsultaResponseDTO criada = consultaService.create(createRequest(meuPaciente.getId(), meuProfissional.getId()));

        Consulta persistida = consultaRepository.findById(criada.getId()).orElseThrow();
        assertThat(persistida.getOrganizacao().getId()).isEqualTo(minhaOrganizacao.getId());
    }

    @Test
    void naoDeveCriarConsultaParaPacienteDeOutraOrganizacao() {
        assertThatThrownBy(() -> consultaService.create(createRequest(pacienteAlheio.getId(), meuProfissional.getId())))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void naoDeveCriarConsultaComProfissionalDeOutraOrganizacao() {
        assertThatThrownBy(() -> consultaService.create(createRequest(meuPaciente.getId(), profissionalAlheio.getId())))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void naoDeveCriarConsultaComProfissionalInexistente() {
        assertThatThrownBy(() -> consultaService.create(createRequest(meuPaciente.getId(), 999_999L)))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void deveEditarConsultaDaPropriaOrganizacao() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional);

        ConsultaResponseDTO atualizada = consultaService.update(consulta.getId(), updateRequest(meuProfissional.getId()));

        assertThat(atualizada.getStatus()).isEqualTo(StatusConsulta.CONFIRMADA);
        assertThat(atualizada.getTipo()).isEqualTo(TipoConsulta.AVALIACAO);
    }

    @Test
    void deveReatribuirProfissionalDaPropriaOrganizacaoNoUpdate() {
        Usuario outroProfissionalMesmaOrg = novoProfissionalVinculado(minhaOrganizacao, "Carla Nutri");
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional);

        ConsultaResponseDTO atualizada = consultaService.update(consulta.getId(), updateRequest(outroProfissionalMesmaOrg.getId()));

        assertThat(atualizada.getProfissionalId()).isEqualTo(outroProfissionalMesmaOrg.getId());
    }

    @Test
    void naoDeveEditarComProfissionalDeOutraOrganizacao() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional);

        assertThatThrownBy(() -> consultaService.update(consulta.getId(), updateRequest(profissionalAlheio.getId())))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThat(consultaRepository.findById(consulta.getId()).orElseThrow().getProfissional().getId())
                .isEqualTo(meuProfissional.getId());
    }

    @Test
    void naoDeveAcessarConsultaDeOutraOrganizacao() {
        Consulta alheia = novaConsulta(outraOrganizacao, pacienteAlheio, profissionalAlheio);

        assertThatThrownBy(() -> consultaService.findById(alheia.getId()))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void naoDeveEditarConsultaDeOutraOrganizacao() {
        Consulta alheia = novaConsulta(outraOrganizacao, pacienteAlheio, profissionalAlheio);

        assertThatThrownBy(() -> consultaService.update(alheia.getId(), updateRequest(meuProfissional.getId())))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        assertThat(consultaRepository.findById(alheia.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusConsulta.AGENDADA);
    }

    @Test
    void naoDeveExcluirConsultaDeOutraOrganizacao() {
        Consulta alheia = novaConsulta(outraOrganizacao, pacienteAlheio, profissionalAlheio);

        assertThatThrownBy(() -> consultaService.delete(alheia.getId()))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        assertThat(consultaRepository.findById(alheia.getId())).isPresent();
    }

    @Test
    void deveFiltrarPorPaciente() {
        novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional);
        Paciente outroPacienteMesmaOrg = novoPaciente(minhaOrganizacao, "Carla da Minha Clinica");
        novaConsulta(minhaOrganizacao, outroPacienteMesmaOrg, meuProfissional);

        List<ConsultaResponseDTO> resultado = consultaService.findAll(meuPaciente.getId(), null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getPacienteId()).isEqualTo(meuPaciente.getId());
    }

    @Test
    void deveFiltrarPorNomeDoPacienteCaseInsensitive() {
        novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional);
        Paciente carla = novoPaciente(minhaOrganizacao, "Carla da Minha Clinica");
        novaConsulta(minhaOrganizacao, carla, meuProfissional);

        List<ConsultaResponseDTO> resultado = consultaService.findAll(null, "ana");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getPacienteNome()).isEqualTo("Ana da Minha Clinica");
    }

    @Test
    void deveIgnorarBuscaEmBranco() {
        novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional);

        List<ConsultaResponseDTO> resultado = consultaService.findAll(null, "   ");

        assertThat(resultado).hasSize(1);
    }

    @Test
    void deveListarApenasProfissionaisDaOrganizacaoAtual() {
        List<ProfissionalResponseDTO> resultado = consultaService.listarProfissionais();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getId()).isEqualTo(meuProfissional.getId());
    }
}
