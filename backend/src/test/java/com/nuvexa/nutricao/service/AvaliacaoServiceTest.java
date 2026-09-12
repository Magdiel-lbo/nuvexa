package com.nuvexa.nutricao.service;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.service.ContextoDeAutenticacao;
import com.nuvexa.core.service.OrganizacaoScopedContext;
import com.nuvexa.core.service.ValidadorOrganizacional;
import com.nuvexa.nutricao.dto.request.AvaliacaoCreateRequestDTO;
import com.nuvexa.nutricao.model.Avaliacao;
import com.nuvexa.nutricao.model.StatusAvaliacao;
import com.nuvexa.nutricao.model.TipoAvaliacao;
import com.nuvexa.nutricao.dto.request.AvaliacaoUpdateRequestDTO;
import com.nuvexa.nutricao.dto.response.AvaliacaoResponseDTO;
import com.nuvexa.nutricao.repository.AvaliacaoRepository;
import com.nuvexa.platform.auditoria.AuditoriaService;
import com.nuvexa.platform.auditoria.EntidadeAuditavel;
import com.nuvexa.platform.auditoria.TipoEventoAuditoria;
import com.nuvexa.platform.exception.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cobre a fábrica {@link AvaliacaoService#criarMedidaRapida} e a validação de data futura
 * introduzidas no Passo 1. As buscas por QueryDSL (buscarUltimaAvaliacaoComPeso /
 * buscarPesosMaisRecentesPorPaciente) precisam de EntityManager real — não são unit-testáveis
 * aqui; ficam cobertas por verificação manual contra o banco (ver histórico da conversa) até que
 * a infraestrutura de teste de integração seja atualizada para o OrganizacaoScopedContext (dívida
 * pré-existente, fora do escopo deste passo).
 */
class AvaliacaoServiceTest {

    private static final Long ORGANIZACAO_ATUAL_ID = 7L;

    private AvaliacaoRepository avaliacaoRepository;
    private ValidadorOrganizacional validadorOrganizacional;
    private ModelMapper modelMapper;
    private OrganizacaoScopedContext contexto;
    private ContextoDeAutenticacao contextoDeAutenticacao;
    private MessageSourceAccessor mensagens;
    private AuditoriaService auditoriaService;

    private Organizacao organizacaoAtual;
    private AvaliacaoService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        avaliacaoRepository = mock(AvaliacaoRepository.class);
        validadorOrganizacional = mock(ValidadorOrganizacional.class);
        modelMapper = mock(ModelMapper.class);
        contexto = mock(OrganizacaoScopedContext.class);
        contextoDeAutenticacao = mock(ContextoDeAutenticacao.class);
        mensagens = mock(MessageSourceAccessor.class);
        auditoriaService = mock(AuditoriaService.class);

        organizacaoAtual = organizacao(ORGANIZACAO_ATUAL_ID, "Clínica Atual");

        when(contexto.getContextoDeAutenticacao()).thenReturn(contextoDeAutenticacao);
        when(contexto.getMensagens()).thenReturn(mensagens);
        when(contextoDeAutenticacao.organizacaoAtual()).thenReturn(organizacaoAtual);
        when(contextoDeAutenticacao.organizacaoAtualId()).thenReturn(ORGANIZACAO_ATUAL_ID);
        when(contextoDeAutenticacao.usuarioAtual()).thenReturn(usuario(99L, "Usuário Logado"));
        when(mensagens.getMessage(any(String.class), any(Object[].class))).thenAnswer(invocation -> invocation.getArgument(0));

        service = new AvaliacaoService(avaliacaoRepository, modelMapper, contexto, auditoriaService, validadorOrganizacional);
    }

    private Organizacao organizacao(Long id, String nome) {
        Organizacao organizacao = Organizacao.builder().nome(nome).tipo(TipoOrganizacao.CLINICA).status(StatusOrganizacao.ATIVA).build();
        organizacao.setId(id);
        return organizacao;
    }

    private Paciente paciente(Long id) {
        Paciente paciente = Paciente.builder()
                .organizacao(organizacaoAtual)
                .nome("Maria Souza")
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build();
        paciente.setId(id);
        return paciente;
    }

    private Usuario usuario(Long id, String nome) {
        Usuario usuario = Usuario.builder().nome(nome).email("x" + id + "@nuvexa.com").senha("hash").perfil(Perfil.PROFISSIONAL).ativo(true).build();
        usuario.setId(id);
        return usuario;
    }

    private Avaliacao avaliacao(Long id, Paciente paciente, Usuario avaliador, StatusAvaliacao status, BigDecimal peso) {
        Avaliacao avaliacao = Avaliacao.builder()
                .organizacao(organizacaoAtual)
                .paciente(paciente)
                .avaliador(avaliador)
                .data(LocalDate.now())
                .tipo(TipoAvaliacao.ANTROPOMETRIA)
                .status(status)
                .peso(peso)
                .build();
        avaliacao.setId(id);
        return avaliacao;
    }

    private AvaliacaoUpdateRequestDTO updateRequest(Long avaliadorId, StatusAvaliacao status, BigDecimal peso) {
        return AvaliacaoUpdateRequestDTO.builder()
                .avaliadorId(avaliadorId)
                .data(LocalDate.now())
                .tipo(TipoAvaliacao.ANTROPOMETRIA)
                .status(status)
                .peso(peso)
                .build();
    }

    @Test
    void naoDeveCriarAvaliacaoComDataFuturaEPesoPreenchido() {
        Paciente paciente = paciente(1L);
        Usuario avaliador = usuario(2L, "Joana Nutri");
        when(validadorOrganizacional.pacienteDaOrganizacao(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(avaliador));

        AvaliacaoCreateRequestDTO request = AvaliacaoCreateRequestDTO.builder()
                .pacienteId(1L)
                .avaliadorId(2L)
                .data(LocalDate.now().plusDays(1))
                .tipo(TipoAvaliacao.ANTROPOMETRIA)
                .status(StatusAvaliacao.CONCLUIDA)
                .peso(new BigDecimal("70.00"))
                .build();

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void devePermitirDataFuturaQuandoAvaliacaoAindaNaoTemPeso() {
        Paciente paciente = paciente(1L);
        Usuario avaliador = usuario(2L, "Joana Nutri");
        when(validadorOrganizacional.pacienteDaOrganizacao(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(avaliador));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvaliacaoCreateRequestDTO request = AvaliacaoCreateRequestDTO.builder()
                .pacienteId(1L)
                .avaliadorId(2L)
                .data(LocalDate.now().plusDays(1))
                .tipo(TipoAvaliacao.ANTROPOMETRIA)
                .status(StatusAvaliacao.AGENDADA)
                .build();

        service.create(request);
    }

    @Test
    void criarMedidaRapidaDeveMontarAvaliacaoAntropometricaConcluida() {
        Paciente paciente = paciente(1L);
        Usuario avaliador = usuario(2L, "Joana Nutri");
        LocalDate hoje = LocalDate.now();

        Avaliacao avaliacao = AvaliacaoService.criarMedidaRapida(organizacaoAtual, paciente, avaliador, hoje, new BigDecimal("58.00"));

        assertThat(avaliacao.getOrganizacao()).isEqualTo(organizacaoAtual);
        assertThat(avaliacao.getPaciente()).isEqualTo(paciente);
        assertThat(avaliacao.getAvaliador()).isEqualTo(avaliador);
        assertThat(avaliacao.getData()).isEqualTo(hoje);
        assertThat(avaliacao.getTipo()).isEqualTo(TipoAvaliacao.ANTROPOMETRIA);
        assertThat(avaliacao.getStatus()).isEqualTo(StatusAvaliacao.CONCLUIDA);
        assertThat(avaliacao.getPeso()).isEqualByComparingTo("58.00");
        assertThat(avaliacao.getPercentualGordura()).isNull();
    }

    @Test
    void naoDevePermitirEditarAvaliacaoConcluida() {
        Paciente paciente = paciente(1L);
        Usuario avaliador = usuario(2L, "Joana Nutri");
        Avaliacao existente = avaliacao(10L, paciente, avaliador, StatusAvaliacao.CONCLUIDA, new BigDecimal("70.00"));
        when(avaliacaoRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.update(10L, updateRequest(2L, StatusAvaliacao.CONCLUIDA, new BigDecimal("65.00"))))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(validadorOrganizacional, never()).usuarioAtivoNaOrganizacao(eq(2L), any());
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void devePermitirEditarAvaliacaoAgendada() {
        Paciente paciente = paciente(1L);
        Usuario avaliador = usuario(2L, "Joana Nutri");
        Avaliacao existente = avaliacao(10L, paciente, avaliador, StatusAvaliacao.AGENDADA, null);
        when(avaliacaoRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(avaliador));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvaliacaoResponseDTO resultado = service.update(10L, updateRequest(2L, StatusAvaliacao.AGENDADA, null));

        assertThat(resultado).isNotNull();
        verify(avaliacaoRepository).save(existente);
    }

    @Test
    void deveRegistrarEventoCriacaoNaAuditoria() {
        Paciente paciente = paciente(1L);
        Usuario avaliador = usuario(2L, "Joana Nutri");
        when(validadorOrganizacional.pacienteDaOrganizacao(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(avaliador));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(invocation -> {
            Avaliacao avaliacao = invocation.getArgument(0);
            avaliacao.setId(10L);
            return avaliacao;
        });

        AvaliacaoCreateRequestDTO request = AvaliacaoCreateRequestDTO.builder()
                .pacienteId(1L)
                .avaliadorId(2L)
                .data(LocalDate.now())
                .tipo(TipoAvaliacao.ANTROPOMETRIA)
                .status(StatusAvaliacao.AGENDADA)
                .build();

        service.create(request);

        verify(auditoriaService).registrar(eq(EntidadeAuditavel.AVALIACAO), eq(10L), eq(TipoEventoAuditoria.CRIACAO),
                eq(ORGANIZACAO_ATUAL_ID), eq(99L), eq("Usuário Logado"), isNull(), any(String.class));
    }

    @Test
    void deveRegistrarEventoEdicaoNaAuditoria() {
        Paciente paciente = paciente(1L);
        Usuario avaliador = usuario(2L, "Joana Nutri");
        Avaliacao existente = avaliacao(10L, paciente, avaliador, StatusAvaliacao.AGENDADA, null);
        when(avaliacaoRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(avaliador));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(10L, updateRequest(2L, StatusAvaliacao.AGENDADA, null));

        verify(auditoriaService).registrar(eq(EntidadeAuditavel.AVALIACAO), eq(10L), eq(TipoEventoAuditoria.EDICAO),
                eq(ORGANIZACAO_ATUAL_ID), eq(99L), eq("Usuário Logado"), any(String.class), any(String.class));
    }

    @Test
    void deveRegistrarEventoExclusaoNaAuditoria() {
        Paciente paciente = paciente(1L);
        Usuario avaliador = usuario(2L, "Joana Nutri");
        Avaliacao existente = avaliacao(10L, paciente, avaliador, StatusAvaliacao.CONCLUIDA, new BigDecimal("70.00"));
        when(avaliacaoRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        service.delete(10L);

        verify(auditoriaService).registrar(eq(EntidadeAuditavel.AVALIACAO), eq(10L), eq(TipoEventoAuditoria.EXCLUSAO),
                eq(ORGANIZACAO_ATUAL_ID), eq(99L), eq("Usuário Logado"), any(String.class), isNull());
    }
}
