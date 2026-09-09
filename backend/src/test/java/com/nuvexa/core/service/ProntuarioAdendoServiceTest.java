package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.ProntuarioAdendoCreateRequestDTO;
import com.nuvexa.core.dto.response.ProntuarioAdendoResponseDTO;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.PapelOrganizacional;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.ProntuarioAdendo;
import com.nuvexa.core.model.SecaoProntuario;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.StatusProntuario;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.ProntuarioAdendoRepository;
import com.nuvexa.core.repository.ProntuarioRepository;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.platform.auditoria.AuditoriaService;
import com.nuvexa.platform.auditoria.EntidadeAuditavel;
import com.nuvexa.platform.auditoria.TipoEventoAuditoria;
import com.nuvexa.platform.exception.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProntuarioAdendoServiceTest {

    private static final Long ORGANIZACAO_ATUAL_ID = 7L;

    private ProntuarioAdendoRepository prontuarioAdendoRepository;
    private ProntuarioRepository prontuarioRepository;
    private VinculoRepository vinculoRepository;
    private AuditoriaService auditoriaService;
    private OrganizacaoScopedContext contexto;
    private ContextoDeAutenticacao contextoDeAutenticacao;
    private MessageSourceAccessor mensagens;

    private Organizacao organizacaoAtual;
    private ProntuarioAdendoService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        prontuarioAdendoRepository = mock(ProntuarioAdendoRepository.class);
        prontuarioRepository = mock(ProntuarioRepository.class);
        vinculoRepository = mock(VinculoRepository.class);
        auditoriaService = mock(AuditoriaService.class);
        contexto = mock(OrganizacaoScopedContext.class);
        contextoDeAutenticacao = mock(ContextoDeAutenticacao.class);
        mensagens = mock(MessageSourceAccessor.class);

        organizacaoAtual = organizacao(ORGANIZACAO_ATUAL_ID, "Clínica Atual");

        when(contexto.getContextoDeAutenticacao()).thenReturn(contextoDeAutenticacao);
        when(contexto.getMensagens()).thenReturn(mensagens);
        when(contextoDeAutenticacao.organizacaoAtual()).thenReturn(organizacaoAtual);
        when(contextoDeAutenticacao.organizacaoAtualId()).thenReturn(ORGANIZACAO_ATUAL_ID);
        when(contextoDeAutenticacao.usuarioAtual()).thenReturn(usuario(99L, "Usuário Logado"));
        when(mensagens.getMessage(any(String.class), any(Object[].class))).thenAnswer(invocation -> invocation.getArgument(0));

        service = new ProntuarioAdendoService(prontuarioAdendoRepository, prontuarioRepository, vinculoRepository, auditoriaService, contexto);
    }

    private Organizacao organizacao(Long id, String nome) {
        Organizacao organizacao = Organizacao.builder().nome(nome).tipo(TipoOrganizacao.CLINICA).status(StatusOrganizacao.ATIVA).build();
        organizacao.setId(id);
        return organizacao;
    }

    private Usuario usuario(Long id, String nome) {
        Usuario usuario = Usuario.builder().nome(nome).email("x" + id + "@nuvexa.com").senha("hash").perfil(Perfil.PROFISSIONAL).ativo(true).build();
        usuario.setId(id);
        return usuario;
    }

    private Vinculo vinculo(Usuario usuario, Organizacao organizacao, boolean ativo) {
        return Vinculo.builder().usuario(usuario).organizacao(organizacao).papel(PapelOrganizacional.MEMBRO).ativo(ativo).build();
    }

    private Prontuario prontuario(Long id, StatusProntuario status, Usuario autor) {
        Paciente paciente = Paciente.builder()
                .organizacao(organizacaoAtual)
                .nome("Maria Souza")
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build();
        paciente.setId(1L);

        Prontuario prontuario = Prontuario.builder()
                .organizacao(organizacaoAtual)
                .paciente(paciente)
                .autor(autor)
                .secao(SecaoProntuario.EVOLUCAO)
                .status(status)
                .conteudo("conteúdo do prontuário")
                .comAnexo(false)
                .build();
        prontuario.setId(id);
        return prontuario;
    }

    private ProntuarioAdendoCreateRequestDTO request(Long autorId, String texto) {
        return ProntuarioAdendoCreateRequestDTO.builder().autorId(autorId).texto(texto).build();
    }

    @Test
    void devePermitirCriarAdendoParaProntuarioAssinado() {
        Usuario autorProntuario = usuario(2L, "Joana Nutri");
        Usuario autorAdendo = usuario(3L, "Carlos Nutri");
        Prontuario prontuario = prontuario(10L, StatusProntuario.ASSINADO, autorProntuario);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(3L)).thenReturn(List.of(vinculo(autorAdendo, organizacaoAtual, true)));
        when(prontuarioAdendoRepository.save(any(ProntuarioAdendo.class))).thenAnswer(invocation -> {
            ProntuarioAdendo adendo = invocation.getArgument(0);
            adendo.setId(55L);
            return adendo;
        });

        ProntuarioAdendoResponseDTO resultado = service.create(10L, request(3L, "Correção do peso registrado."));

        assertThat(resultado.getId()).isEqualTo(55L);
        assertThat(resultado.getProntuarioId()).isEqualTo(10L);
        assertThat(resultado.getAutorNome()).isEqualTo("Carlos Nutri");
    }

    @Test
    void naoDevePermitirCriarAdendoParaRascunho() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario prontuario = prontuario(10L, StatusProntuario.RASCUNHO, autor);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));

        assertThatThrownBy(() -> service.create(10L, request(2L, "Correção.")))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(prontuarioAdendoRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirCriarAdendoParaPendente() {
        Usuario autor = usuario(2L, "Joana Nutri");
        Prontuario prontuario = prontuario(10L, StatusProntuario.PENDENTE, autor);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));

        assertThatThrownBy(() -> service.create(10L, request(2L, "Correção.")))
                .isInstanceOf(NegocioException.class);
        verify(prontuarioAdendoRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirTextoVazioOuSoComEspacos() {
        assertThatThrownBy(() -> service.create(10L, request(2L, "   ")))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(prontuarioAdendoRepository, never()).save(any());
        verify(prontuarioRepository, never()).findByIdAndOrganizacaoId(anyLong(), anyLong());
    }

    @Test
    void naoDevePermitirAutorInvalido() {
        Usuario autorProntuario = usuario(2L, "Joana Nutri");
        Prontuario prontuario = prontuario(10L, StatusProntuario.ASSINADO, autorProntuario);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(999L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.create(10L, request(999L, "Correção.")))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(prontuarioAdendoRepository, never()).save(any());
    }

    @Test
    void prontuarioOriginalPermaneceInalteradoAposCriarAdendo() {
        Usuario autorProntuario = usuario(2L, "Joana Nutri");
        Usuario autorAdendo = usuario(3L, "Carlos Nutri");
        Prontuario prontuario = prontuario(10L, StatusProntuario.ASSINADO, autorProntuario);
        String conteudoOriginal = prontuario.getConteudo();
        StatusProntuario statusOriginal = prontuario.getStatus();
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(3L)).thenReturn(List.of(vinculo(autorAdendo, organizacaoAtual, true)));
        when(prontuarioAdendoRepository.save(any(ProntuarioAdendo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(10L, request(3L, "Correção do peso registrado."));

        assertThat(prontuario.getConteudo()).isEqualTo(conteudoOriginal);
        assertThat(prontuario.getStatus()).isEqualTo(statusOriginal);
        assertThat(prontuario.getAutor()).isEqualTo(autorProntuario);
        verify(prontuarioRepository, never()).save(any());
    }

    @Test
    void deveRegistrarEventoAdendoNaAuditoriaDoProntuarioOriginal() {
        Usuario autorProntuario = usuario(2L, "Joana Nutri");
        Usuario autorAdendo = usuario(3L, "Carlos Nutri");
        Prontuario prontuario = prontuario(10L, StatusProntuario.ASSINADO, autorProntuario);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(3L)).thenReturn(List.of(vinculo(autorAdendo, organizacaoAtual, true)));
        when(prontuarioAdendoRepository.save(any(ProntuarioAdendo.class))).thenAnswer(invocation -> {
            ProntuarioAdendo adendo = invocation.getArgument(0);
            adendo.setId(55L);
            return adendo;
        });

        service.create(10L, request(3L, "Correção do peso registrado."));

        verify(auditoriaService).registrar(eq(EntidadeAuditavel.PRONTUARIO), eq(10L), eq(TipoEventoAuditoria.ADENDO),
                eq(ORGANIZACAO_ATUAL_ID), eq(99L), eq("Usuário Logado"), eq(null), any(String.class));
    }

    @Test
    void adendoServiceNaoExpoeAtualizarOuExcluir() {
        Method[] metodos = ProntuarioAdendoService.class.getDeclaredMethods();
        boolean temOperacaoDeMutacao = Arrays.stream(metodos)
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .map(String::toLowerCase)
                .anyMatch(nome -> nome.contains("atualiz") || nome.contains("exclu") || nome.contains("delet") || nome.contains("updat"));

        assertThat(temOperacaoDeMutacao).isFalse();
    }
}
