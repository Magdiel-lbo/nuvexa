package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.ConsultaCreateRequestDTO;
import com.nuvexa.core.dto.request.ConsultaUpdateRequestDTO;
import com.nuvexa.core.dto.response.ConsultaResponseDTO;
import com.nuvexa.core.dto.response.ProfissionalResponseDTO;
import com.nuvexa.core.model.Consulta;
import com.nuvexa.core.model.ConsultaStatusHistorico;
import com.nuvexa.core.model.StatusConsulta;
import com.nuvexa.core.model.TipoConsulta;
import com.nuvexa.core.repository.ConsultaRepository;
import com.nuvexa.core.repository.ConsultaStatusHistoricoRepository;
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
@Import({QuerydslConfig.class, MessageConfig.class, ModelMapperConfig.class, OrganizacaoScopedContext.class, ConsultaService.class, ValidadorOrganizacional.class})
class ConsultaEscopoOrganizacionalIntegrationTest {

    @Autowired
    private ConsultaService consultaService;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private ConsultaStatusHistoricoRepository consultaStatusHistoricoRepository;

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
    private Usuario usuarioLogado;

    @BeforeEach
    void setUp() {
        minhaOrganizacao = novaOrganizacao("Clínica A");
        outraOrganizacao = novaOrganizacao("Clínica B");
        meuPaciente = novoPaciente(minhaOrganizacao, "Ana da Minha Clinica");
        pacienteAlheio = novoPaciente(outraOrganizacao, "Bruno da Outra Clinica");
        meuProfissional = novoProfissionalVinculado(minhaOrganizacao, "Joana Nutri");
        profissionalAlheio = novoProfissionalVinculado(outraOrganizacao, "Carlos Nutri");
        // Usuário autenticado sem vínculo/perfil de profissional de propósito: alteradoPor deve
        // ser sempre quem chamou a operação, não precisa ser o Consulta.profissional nem aparecer
        // em listarProfissionais() (que já tem teste próprio contando exatamente 1 resultado).
        usuarioLogado = novoUsuario("Usuária Logada");
        estarLogadoEm(minhaOrganizacao);
        when(contextoDeAutenticacao.usuarioAtual()).thenReturn(usuarioLogado);
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
        Usuario usuario = novoUsuario(nome);
        vinculoRepository.saveAndFlush(Vinculo.builder()
                .usuario(usuario)
                .organizacao(organizacao)
                .papel(PapelOrganizacional.MEMBRO)
                .ativo(true)
                .build());
        return usuario;
    }

    private Usuario novoUsuario(String nome) {
        return usuarioRepository.saveAndFlush(Usuario.builder()
                .nome(nome)
                .email(nome.toLowerCase().replace(" ", ".") + "@nuvexa.com")
                .senha("hash")
                .perfil(Perfil.PROFISSIONAL)
                .ativo(true)
                .build());
    }

    private Consulta novaConsulta(Organizacao organizacao, Paciente paciente, Usuario profissional) {
        return novaConsulta(organizacao, paciente, profissional,
                LocalDateTime.of(2026, 10, 9, 14, 30), 30, StatusConsulta.AGENDADA);
    }

    private Consulta novaConsulta(Organizacao organizacao, Paciente paciente, Usuario profissional,
            LocalDateTime dataHora, Integer duracaoMinutos, StatusConsulta status) {
        return consultaRepository.saveAndFlush(Consulta.builder()
                .organizacao(organizacao)
                .paciente(paciente)
                .profissional(profissional)
                .dataHora(dataHora)
                .duracaoMinutos(duracaoMinutos)
                .tipo(TipoConsulta.RETORNO)
                .status(status)
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

    private ConsultaUpdateRequestDTO updateRequest(Long profissionalId, StatusConsulta status, String motivoTransicao) {
        ConsultaUpdateRequestDTO request = updateRequest(profissionalId);
        request.setStatus(status);
        request.setMotivoTransicao(motivoTransicao);
        return request;
    }

    private List<ConsultaStatusHistorico> historicoDe(Consulta consulta) {
        return consultaStatusHistoricoRepository
                .findByConsultaIdAndOrganizacaoIdOrderByCriadoEmAsc(consulta.getId(), minhaOrganizacao.getId());
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
        // flush explícito é o que expõe o bug do ModelMapper de "identifier was altered": sem
        // isso o teste passa mesmo com o bug, porque o dirty-check só roda no flush/commit real
        // (que uma requisição HTTP sempre faz, mas o rollback automático do @DataJpaTest pode
        // nunca disparar).
        consultaRepository.flush();

        assertThat(atualizada.getProfissionalId()).isEqualTo(outroProfissionalMesmaOrg.getId());
        assertThat(consultaRepository.findById(consulta.getId()).orElseThrow().getProfissional().getId())
                .isEqualTo(outroProfissionalMesmaOrg.getId());
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

    @Test
    void deveCriarConsultaComHorarioAdjacenteSemConflito() {
        novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);

        ConsultaCreateRequestDTO request = createRequest(meuPaciente.getId(), meuProfissional.getId());
        request.setDataHora(LocalDateTime.of(2026, 11, 2, 9, 30));
        request.setDuracaoMinutos(30);

        ConsultaResponseDTO criada = consultaService.create(request);

        assertThat(criada).isNotNull();
    }

    @Test
    void naoDeveCriarConsultaComConflitoDeProfissional() {
        novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);
        Paciente outroPaciente = novoPaciente(minhaOrganizacao, "Carla da Minha Clinica");

        ConsultaCreateRequestDTO request = createRequest(outroPaciente.getId(), meuProfissional.getId());
        request.setDataHora(LocalDateTime.of(2026, 11, 2, 9, 15));
        request.setDuracaoMinutos(30);

        assertThatThrownBy(() -> consultaService.create(request))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void naoDeveCriarConsultaComConflitoDePaciente() {
        novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);
        Usuario outroProfissional = novoProfissionalVinculado(minhaOrganizacao, "Marcos Nutri");

        ConsultaCreateRequestDTO request = createRequest(meuPaciente.getId(), outroProfissional.getId());
        request.setDataHora(LocalDateTime.of(2026, 11, 2, 9, 15));
        request.setDuracaoMinutos(30);

        assertThatThrownBy(() -> consultaService.create(request))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void consultaCanceladaNaoContaComoConflito() {
        novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.CANCELADA);

        ConsultaCreateRequestDTO request = createRequest(meuPaciente.getId(), meuProfissional.getId());
        request.setDataHora(LocalDateTime.of(2026, 11, 2, 9, 0));
        request.setDuracaoMinutos(30);

        ConsultaResponseDTO criada = consultaService.create(request);

        assertThat(criada).isNotNull();
    }

    @Test
    void consultaComStatusFaltouContaComoConflito() {
        novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.FALTOU);

        ConsultaCreateRequestDTO request = createRequest(meuPaciente.getId(), meuProfissional.getId());
        request.setDataHora(LocalDateTime.of(2026, 11, 2, 9, 0));
        request.setDuracaoMinutos(30);

        assertThatThrownBy(() -> consultaService.create(request))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void deveEditarConsultaMantendoMesmoHorarioSemConflitarComSiMesma() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);

        ConsultaUpdateRequestDTO request = updateRequest(meuProfissional.getId());
        request.setDataHora(LocalDateTime.of(2026, 11, 2, 9, 0));
        request.setDuracaoMinutos(30);

        ConsultaResponseDTO atualizada = consultaService.update(consulta.getId(), request);

        assertThat(atualizada).isNotNull();
    }

    @Test
    void naoDeveEditarConsultaParaHorarioQueColideComOutra() {
        novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);
        Consulta outraConsulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 3, 9, 0), 30, StatusConsulta.AGENDADA);

        ConsultaUpdateRequestDTO request = updateRequest(meuProfissional.getId());
        request.setDataHora(LocalDateTime.of(2026, 11, 2, 9, 15));
        request.setDuracaoMinutos(30);

        assertThatThrownBy(() -> consultaService.update(outraConsulta.getId(), request))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void naoDevePermitirEditarConsultaRealizada() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.REALIZADA);

        assertThatThrownBy(() -> consultaService.update(consulta.getId(), updateRequest(meuProfissional.getId())))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void naoDevePermitirEditarConsultaCancelada() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.CANCELADA);

        assertThatThrownBy(() -> consultaService.update(consulta.getId(), updateRequest(meuProfissional.getId())))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void naoDevePermitirEditarConsultaComFalta() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.FALTOU);

        assertThatThrownBy(() -> consultaService.update(consulta.getId(), updateRequest(meuProfissional.getId())))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void devePermitirEditarConsultaConfirmada() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.CONFIRMADA);

        ConsultaResponseDTO atualizada = consultaService.update(consulta.getId(), updateRequest(meuProfissional.getId()));

        assertThat(atualizada).isNotNull();
    }

    @Test
    void naoDevePermitirExcluirConsultaRealizada() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.REALIZADA);

        assertThatThrownBy(() -> consultaService.delete(consulta.getId()))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThat(consultaRepository.findById(consulta.getId())).isPresent();
    }

    @Test
    void naoDevePermitirExcluirConsultaCancelada() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.CANCELADA);

        assertThatThrownBy(() -> consultaService.delete(consulta.getId()))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThat(consultaRepository.findById(consulta.getId())).isPresent();
    }

    @Test
    void naoDevePermitirExcluirConsultaComFalta() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.FALTOU);

        assertThatThrownBy(() -> consultaService.delete(consulta.getId()))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThat(consultaRepository.findById(consulta.getId())).isPresent();
    }

    @Test
    void devePermitirExcluirConsultaAgendada() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);

        consultaService.delete(consulta.getId());

        assertThat(consultaRepository.findById(consulta.getId())).isEmpty();
    }

    // --- Histórico de transição de status ---

    @Test
    void deveRegistrarTransicaoDeStatusNaCriacao() {
        ConsultaResponseDTO criada = consultaService.create(createRequest(meuPaciente.getId(), meuProfissional.getId()));

        List<ConsultaStatusHistorico> historico = consultaStatusHistoricoRepository
                .findByConsultaIdAndOrganizacaoIdOrderByCriadoEmAsc(criada.getId(), minhaOrganizacao.getId());

        assertThat(historico).hasSize(1);
        assertThat(historico.getFirst().getStatusAnterior()).isNull();
        assertThat(historico.getFirst().getStatusNovo()).isEqualTo(StatusConsulta.AGENDADA);
        assertThat(historico.getFirst().getAlteradoPor().getId()).isEqualTo(usuarioLogado.getId());
        assertThat(historico.getFirst().getOrganizacao().getId()).isEqualTo(minhaOrganizacao.getId());
    }

    @Test
    void devePermitirExcluirConsultaCriadaSemNenhumaTransicaoDeStatus() {
        // A linha de criação (null->status inicial) sozinha não deve bloquear exclusão — só uma
        // transição real (status mudou de um valor pra outro) conta, ver garantirSemHistorico.
        ConsultaResponseDTO criada = consultaService.create(createRequest(meuPaciente.getId(), meuProfissional.getId()));

        consultaService.delete(criada.getId());

        assertThat(consultaRepository.findById(criada.getId())).isEmpty();
    }

    @Test
    void deveRegistrarExatamenteUmaTransicaoQuandoStatusMuda() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);

        consultaService.update(consulta.getId(), updateRequest(meuProfissional.getId(), StatusConsulta.CONFIRMADA, null));

        List<ConsultaStatusHistorico> historico = historicoDe(consulta);
        assertThat(historico).hasSize(1);
        assertThat(historico.getFirst().getStatusAnterior()).isEqualTo(StatusConsulta.AGENDADA);
        assertThat(historico.getFirst().getStatusNovo()).isEqualTo(StatusConsulta.CONFIRMADA);
        assertThat(historico.getFirst().getOrganizacao().getId()).isEqualTo(minhaOrganizacao.getId());
    }

    @Test
    void naoDeveRegistrarHistoricoQuandoStatusNaoMuda() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);

        ConsultaUpdateRequestDTO request = updateRequest(meuProfissional.getId(), StatusConsulta.AGENDADA, null);
        request.setObservacoes("Só uma observação nova, sem mudar status");
        consultaService.update(consulta.getId(), request);

        assertThat(historicoDe(consulta)).isEmpty();
    }

    @Test
    void devePreservarSequenciaCompletaEmMultiplasTransicoes() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);

        consultaService.update(consulta.getId(), updateRequest(meuProfissional.getId(), StatusConsulta.CONFIRMADA, null));
        consultaService.update(consulta.getId(), updateRequest(meuProfissional.getId(), StatusConsulta.REALIZADA, null));

        List<ConsultaStatusHistorico> historico = historicoDe(consulta);
        assertThat(historico).hasSize(2);
        assertThat(historico.get(0).getStatusAnterior()).isEqualTo(StatusConsulta.AGENDADA);
        assertThat(historico.get(0).getStatusNovo()).isEqualTo(StatusConsulta.CONFIRMADA);
        assertThat(historico.get(1).getStatusAnterior()).isEqualTo(StatusConsulta.CONFIRMADA);
        assertThat(historico.get(1).getStatusNovo()).isEqualTo(StatusConsulta.REALIZADA);
    }

    @Test
    void deveExigirMotivoTransicaoParaCancelar() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);

        assertThatThrownBy(() -> consultaService.update(consulta.getId(),
                updateRequest(meuProfissional.getId(), StatusConsulta.CANCELADA, null)))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThat(historicoDe(consulta)).isEmpty();
        assertThat(consultaRepository.findById(consulta.getId()).orElseThrow().getStatus()).isEqualTo(StatusConsulta.AGENDADA);
    }

    @Test
    void deveExigirMotivoTransicaoParaFalta() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);

        assertThatThrownBy(() -> consultaService.update(consulta.getId(),
                updateRequest(meuProfissional.getId(), StatusConsulta.FALTOU, "   ")))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThat(historicoDe(consulta)).isEmpty();
    }

    @Test
    void devePermitirCancelarComMotivoTransicaoPreenchido() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);

        consultaService.update(consulta.getId(),
                updateRequest(meuProfissional.getId(), StatusConsulta.CANCELADA, "Paciente remarcou por telefone"));

        List<ConsultaStatusHistorico> historico = historicoDe(consulta);
        assertThat(historico).hasSize(1);
        assertThat(historico.getFirst().getStatusNovo()).isEqualTo(StatusConsulta.CANCELADA);
        assertThat(historico.getFirst().getMotivoTransicao()).isEqualTo("Paciente remarcou por telefone");
    }

    @Test
    void deveAceitarMotivoTransicaoOpcionalParaOutrasTransicoes() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);

        consultaService.update(consulta.getId(), updateRequest(meuProfissional.getId(), StatusConsulta.CONFIRMADA, null));

        List<ConsultaStatusHistorico> historico = historicoDe(consulta);
        assertThat(historico).hasSize(1);
        assertThat(historico.getFirst().getMotivoTransicao()).isNull();
    }

    @Test
    void alteradoPorDeveSerOUsuarioAutenticadoQueRealizouAOperacao() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);

        consultaService.update(consulta.getId(), updateRequest(meuProfissional.getId(), StatusConsulta.CONFIRMADA, null));

        // alteradoPor é quem chamou a operação (usuarioLogado), não o profissional da consulta.
        assertThat(historicoDe(consulta).getFirst().getAlteradoPor().getId()).isEqualTo(usuarioLogado.getId());
        assertThat(usuarioLogado.getId()).isNotEqualTo(meuProfissional.getId());
    }

    @Test
    void naoDeveEnxergarHistoricoDeConsultaDeOutraOrganizacao() {
        Consulta alheia = novaConsulta(outraOrganizacao, pacienteAlheio, profissionalAlheio,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);
        consultaStatusHistoricoRepository.saveAndFlush(ConsultaStatusHistorico.builder()
                .consulta(alheia)
                .organizacao(outraOrganizacao)
                .statusAnterior(null)
                .statusNovo(StatusConsulta.AGENDADA)
                .alteradoPor(profissionalAlheio)
                .build());

        List<ConsultaStatusHistorico> historicoVistoDaMinhaOrganizacao = consultaStatusHistoricoRepository
                .findByConsultaIdAndOrganizacaoIdOrderByCriadoEmAsc(alheia.getId(), minhaOrganizacao.getId());

        assertThat(historicoVistoDaMinhaOrganizacao).isEmpty();
    }

    @Test
    void naoDevePermitirExcluirConsultaComHistoricoDeTransicaoDeStatus() {
        Consulta consulta = novaConsulta(minhaOrganizacao, meuPaciente, meuProfissional,
                LocalDateTime.of(2026, 11, 2, 9, 0), 30, StatusConsulta.AGENDADA);
        consultaService.update(consulta.getId(), updateRequest(meuProfissional.getId(), StatusConsulta.CONFIRMADA, null));

        assertThatThrownBy(() -> consultaService.delete(consulta.getId()))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThat(consultaRepository.findById(consulta.getId())).isPresent();
    }
}
