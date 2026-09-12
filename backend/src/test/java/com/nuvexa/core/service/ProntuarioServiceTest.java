package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.ProntuarioCreateRequestDTO;
import com.nuvexa.core.dto.request.ProntuarioUpdateRequestDTO;
import com.nuvexa.core.dto.response.ProntuarioResponseDTO;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.SecaoProntuario;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.StatusProntuario;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.repository.ProntuarioAdendoRepository;
import com.nuvexa.core.repository.ProntuarioAnexoRepository;
import com.nuvexa.core.repository.ProntuarioRepository;
import com.nuvexa.platform.auditoria.AuditoriaService;
import com.nuvexa.platform.auditoria.EntidadeAuditavel;
import com.nuvexa.platform.auditoria.TipoEventoAuditoria;
import com.nuvexa.platform.exception.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * Cobre a Fase 1 (integridade do prontuário): edição/exclusão bloqueadas quando ASSINADO,
 * transição de status só via {@link ProntuarioService#assinar}, e validação de conteúdo na
 * assinatura. As buscas por QueryDSL (buscarProntuarios) não são unit-testáveis aqui — mesma
 * dívida pré-existente documentada em AvaliacaoServiceTest.
 */
class ProntuarioServiceTest {

    private static final Long ORGANIZACAO_ATUAL_ID = 7L;

    private ProntuarioRepository prontuarioRepository;
    private ProntuarioAdendoRepository prontuarioAdendoRepository;
    private ProntuarioAnexoRepository prontuarioAnexoRepository;
    private ValidadorOrganizacional validadorOrganizacional;
    private ModelMapper modelMapper;
    private OrganizacaoScopedContext contexto;
    private ContextoDeAutenticacao contextoDeAutenticacao;
    private MessageSourceAccessor mensagens;
    private AuditoriaService auditoriaService;

    private Organizacao organizacaoAtual;
    private ProntuarioService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        prontuarioRepository = mock(ProntuarioRepository.class);
        prontuarioAdendoRepository = mock(ProntuarioAdendoRepository.class);
        prontuarioAnexoRepository = mock(ProntuarioAnexoRepository.class);
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

        service = new ProntuarioService(prontuarioRepository, prontuarioAdendoRepository, prontuarioAnexoRepository,
                modelMapper, contexto, auditoriaService, validadorOrganizacional);
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

    private Prontuario prontuario(Long id, StatusProntuario status, Usuario autor, String conteudo) {
        Prontuario prontuario = Prontuario.builder()
                .organizacao(organizacaoAtual)
                .paciente(paciente(1L))
                .autor(autor)
                .secao(SecaoProntuario.EVOLUCAO)
                .status(status)
                .conteudo(conteudo)
                .comAnexo(false)
                .build();
        prontuario.setId(id);
        return prontuario;
    }

    private ProntuarioUpdateRequestDTO updateRequest(Long autorId, StatusProntuario status, String conteudo) {
        return ProntuarioUpdateRequestDTO.builder()
                .autorId(autorId)
                .secao(SecaoProntuario.EVOLUCAO)
                .status(status)
                .conteudo(conteudo)
                .comAnexo(false)
                .build();
    }

    @Test
    void devePermitirEditarRascunho() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.RASCUNHO, autor, "texto inicial");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(autor));
        when(prontuarioRepository.save(any(Prontuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProntuarioResponseDTO resultado = service.update(10L, updateRequest(2L, StatusProntuario.RASCUNHO, "texto editado"));

        assertThat(resultado).isNotNull();
        verify(prontuarioRepository).save(existente);
    }

    @Test
    void devePermitirEditarPendente() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.PENDENTE, autor, "texto inicial");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(autor));
        when(prontuarioRepository.save(any(Prontuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProntuarioResponseDTO resultado = service.update(10L, updateRequest(2L, StatusProntuario.PENDENTE, "texto editado"));

        assertThat(resultado).isNotNull();
        verify(prontuarioRepository).save(existente);
    }

    @Test
    void deveAssinarProntuarioValidoEGravarAutoriaEData() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Usuario usuarioLogado = usuario(3L, "Ana Assina");
        Prontuario existente = prontuario(10L, StatusProntuario.PENDENTE, autor, "conteúdo válido");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(contextoDeAutenticacao.usuarioAtual()).thenReturn(usuarioLogado);
        when(prontuarioRepository.save(any(Prontuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime antes = LocalDateTime.now();
        ProntuarioResponseDTO resultado = service.assinar(10L);
        LocalDateTime depois = LocalDateTime.now();

        assertThat(resultado.getStatus()).isEqualTo(StatusProntuario.ASSINADO);
        assertThat(existente.getStatus()).isEqualTo(StatusProntuario.ASSINADO);
        assertThat(existente.getAssinadoPor()).isEqualTo(usuarioLogado);
        assertThat(existente.getAssinadoEm()).isBetween(antes, depois);
        assertThat(resultado.getAssinadoPorId()).isEqualTo(3L);
    }

    @Test
    void naoDevePermitirEditarProntuarioAssinado() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.ASSINADO, autor, "texto");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.update(10L, updateRequest(2L, StatusProntuario.ASSINADO, "texto alterado")))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(prontuarioRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirExcluirProntuarioAssinado() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.ASSINADO, autor, "texto");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.delete(10L)).isInstanceOf(NegocioException.class);
        verify(prontuarioRepository, never()).delete(any());
    }

    @Test
    void naoDevePermitirVoltarDeAssinadoParaOutroStatus() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.ASSINADO, autor, "texto");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.update(10L, updateRequest(2L, StatusProntuario.RASCUNHO, "texto")))
                .isInstanceOf(NegocioException.class);
        verify(prontuarioRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirDefinirStatusAssinadoPeloUpdateGenerico() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.PENDENTE, autor, "texto");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.update(10L, updateRequest(2L, StatusProntuario.ASSINADO, "texto")))
                .isInstanceOf(NegocioException.class);
        verify(prontuarioRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirAssinarConteudoVazio() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.PENDENTE, autor, "   ");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.assinar(10L))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(prontuarioRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirAlterarAutorDeProntuarioAssinado() {
        Usuario autorOriginal = usuario(2L, "Joana Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.ASSINADO, autorOriginal, "texto");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.update(10L, updateRequest(4L, StatusProntuario.ASSINADO, "texto")))
                .isInstanceOf(NegocioException.class);
        verify(validadorOrganizacional, never()).usuarioAtivoNaOrganizacao(eq(4L), any());
        verify(prontuarioRepository, never()).save(any());
    }

    @Test
    void deveCriarProntuarioERegistrarEventoCriacao() {
        Paciente paciente = paciente(1L);
        Usuario autor = usuario(2L, "Joana Nutri");
        when(validadorOrganizacional.pacienteDaOrganizacao(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(autor));
        when(prontuarioRepository.save(any(Prontuario.class))).thenAnswer(invocation -> {
            Prontuario salvo = invocation.getArgument(0);
            salvo.setId(10L);
            return salvo;
        });

        ProntuarioCreateRequestDTO request = ProntuarioCreateRequestDTO.builder()
                .pacienteId(1L)
                .autorId(2L)
                .secao(SecaoProntuario.EVOLUCAO)
                .status(StatusProntuario.RASCUNHO)
                .conteudo("texto inicial")
                .comAnexo(false)
                .build();

        ProntuarioResponseDTO resultado = service.create(request);

        assertThat(resultado).isNotNull();
        verify(auditoriaService).registrar(eq(EntidadeAuditavel.PRONTUARIO), eq(10L), eq(TipoEventoAuditoria.CRIACAO),
                eq(ORGANIZACAO_ATUAL_ID), eq(99L), eq("Usuário Logado"), isNull(), any(String.class));
    }

    @Test
    void editarProntuarioDeveRegistrarEventoEdicaoComAntesEDepois() {
        Usuario autorOriginal = usuario(2L, "Joana Nutri");
        Usuario novoAutor = usuario(5L, "Carlos Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.RASCUNHO, autorOriginal, "texto inicial");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(5L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(novoAutor));
        when(prontuarioRepository.save(any(Prontuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(10L, updateRequest(5L, StatusProntuario.RASCUNHO, "texto inicial"));

        ArgumentCaptor<String> antesCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> depoisCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditoriaService).registrar(eq(EntidadeAuditavel.PRONTUARIO), eq(10L), eq(TipoEventoAuditoria.EDICAO),
                eq(ORGANIZACAO_ATUAL_ID), eq(99L), eq("Usuário Logado"), antesCaptor.capture(), depoisCaptor.capture());
        assertThat(antesCaptor.getValue()).contains("autorId=2");
        assertThat(depoisCaptor.getValue()).contains("autorId=5");
        assertThat(antesCaptor.getValue()).isNotEqualTo(depoisCaptor.getValue());
    }

    @Test
    void assinarProntuarioDeveRegistrarEventoAssinatura() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Usuario usuarioLogado = usuario(3L, "Ana Assina");
        Prontuario existente = prontuario(10L, StatusProntuario.PENDENTE, autor, "conteúdo válido");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(contextoDeAutenticacao.usuarioAtual()).thenReturn(usuarioLogado);
        when(prontuarioRepository.save(any(Prontuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.assinar(10L);

        ArgumentCaptor<String> antesCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> depoisCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditoriaService).registrar(eq(EntidadeAuditavel.PRONTUARIO), eq(10L), eq(TipoEventoAuditoria.ASSINATURA),
                eq(ORGANIZACAO_ATUAL_ID), eq(3L), eq("Ana Assina"), antesCaptor.capture(), depoisCaptor.capture());
        assertThat(antesCaptor.getValue()).contains("status=PENDENTE");
        assertThat(depoisCaptor.getValue()).contains("status=ASSINADO");
    }

    @Test
    void excluirRascunhoDeveRegistrarEventoExclusao() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.RASCUNHO, autor, "texto a excluir");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        service.delete(10L);

        verify(prontuarioRepository).delete(existente);
        ArgumentCaptor<String> antesCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditoriaService).registrar(eq(EntidadeAuditavel.PRONTUARIO), eq(10L), eq(TipoEventoAuditoria.EXCLUSAO),
                eq(ORGANIZACAO_ATUAL_ID), eq(99L), eq("Usuário Logado"), antesCaptor.capture(), isNull());
        assertThat(antesCaptor.getValue()).contains("texto a excluir");
    }

    @Test
    void naoDevePermitirCriarProntuarioJaComoAssinado() {
        ProntuarioCreateRequestDTO request = ProntuarioCreateRequestDTO.builder()
                .pacienteId(1L)
                .autorId(2L)
                .secao(SecaoProntuario.EVOLUCAO)
                .status(StatusProntuario.ASSINADO)
                .conteudo("texto")
                .comAnexo(false)
                .build();

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(NegocioException.class);
        verify(prontuarioRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirAssinarProntuarioJaAssinado() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.ASSINADO, autor, "texto");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.assinar(10L)).isInstanceOf(NegocioException.class);
        verify(prontuarioRepository, never()).save(any());
    }

    @Test
    void devePermitirExcluirProntuarioSemAdendoNemAnexo() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.RASCUNHO, autor, "texto a excluir");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(prontuarioAdendoRepository.existsByProntuarioId(10L)).thenReturn(false);
        when(prontuarioAnexoRepository.existsByProntuarioId(10L)).thenReturn(false);

        service.delete(10L);

        verify(prontuarioRepository).delete(existente);
    }

    @Test
    void naoDevePermitirExcluirProntuarioComAdendo() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.RASCUNHO, autor, "texto");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(prontuarioAdendoRepository.existsByProntuarioId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(10L))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(prontuarioRepository, never()).delete(any());
    }

    @Test
    void naoDevePermitirExcluirProntuarioComAnexo() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario existente = prontuario(10L, StatusProntuario.RASCUNHO, autor, "texto");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(prontuarioAdendoRepository.existsByProntuarioId(10L)).thenReturn(false);
        when(prontuarioAnexoRepository.existsByProntuarioId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(10L))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(prontuarioRepository, never()).delete(any());
    }
}
