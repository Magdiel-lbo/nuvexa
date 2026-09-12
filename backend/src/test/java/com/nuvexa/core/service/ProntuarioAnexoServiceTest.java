package com.nuvexa.core.service;

import com.nuvexa.core.dto.response.ProntuarioAnexoDownloadDTO;
import com.nuvexa.core.dto.response.ProntuarioAnexoResponseDTO;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.ProntuarioAnexo;
import com.nuvexa.core.model.SecaoProntuario;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.StatusProntuario;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.repository.ProntuarioAnexoRepository;
import com.nuvexa.core.repository.ProntuarioRepository;
import com.nuvexa.platform.auditoria.AuditoriaService;
import com.nuvexa.platform.auditoria.EntidadeAuditavel;
import com.nuvexa.platform.auditoria.TipoEventoAuditoria;
import com.nuvexa.platform.exception.NegocioException;
import com.nuvexa.platform.storage.StorageException;
import com.nuvexa.platform.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProntuarioAnexoServiceTest {

    private static final Long ORGANIZACAO_ATUAL_ID = 7L;

    private ProntuarioAnexoRepository prontuarioAnexoRepository;
    private ProntuarioRepository prontuarioRepository;
    private StorageService storageService;
    private AuditoriaService auditoriaService;
    private OrganizacaoScopedContext contexto;
    private ContextoDeAutenticacao contextoDeAutenticacao;
    private MessageSourceAccessor mensagens;

    private Organizacao organizacaoAtual;
    private ProntuarioAnexoService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        prontuarioAnexoRepository = mock(ProntuarioAnexoRepository.class);
        prontuarioRepository = mock(ProntuarioRepository.class);
        storageService = mock(StorageService.class);
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

        service = new ProntuarioAnexoService(prontuarioAnexoRepository, prontuarioRepository, storageService, auditoriaService, contexto);
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

    private Prontuario prontuario(Long id, StatusProntuario status) {
        Usuario autor = usuario(2L, "Joana Nutri");
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

    private MockMultipartFile arquivoValido() {
        return new MockMultipartFile("arquivo", "exame.pdf", "application/pdf", "conteúdo do exame".getBytes());
    }

    private ProntuarioAnexo anexo(Long id, Prontuario prontuario, String nomeOriginal, String chaveStorage) {
        return ProntuarioAnexo.builder()
                .id(id)
                .organizacao(organizacaoAtual)
                .prontuario(prontuario)
                .nomeOriginal(nomeOriginal)
                .chaveStorage(chaveStorage)
                .tipoMime("application/pdf")
                .tamanho(100L)
                .criadoPor(usuario(99L, "Usuário Logado"))
                .criadoEm(java.time.LocalDateTime.now())
                .build();
    }

    @Test
    void devePermitirUploadEmRascunho() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.RASCUNHO);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(prontuarioAnexoRepository.save(any(ProntuarioAnexo.class))).thenAnswer(invocation -> {
            ProntuarioAnexo a = invocation.getArgument(0);
            a.setId(55L);
            return a;
        });

        ProntuarioAnexoResponseDTO resultado = service.upload(10L, arquivoValido());

        assertThat(resultado.getId()).isEqualTo(55L);
        assertThat(resultado.getNomeOriginal()).isEqualTo("exame.pdf");
        verify(storageService).upload(anyString(), any(byte[].class), eq("application/pdf"));
    }

    @Test
    void devePermitirUploadEmPendente() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.PENDENTE);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(prontuarioAnexoRepository.save(any(ProntuarioAnexo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProntuarioAnexoResponseDTO resultado = service.upload(10L, arquivoValido());

        assertThat(resultado).isNotNull();
    }

    @Test
    void naoDevePermitirUploadEmAssinado() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.ASSINADO);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));

        assertThatThrownBy(() -> service.upload(10L, arquivoValido()))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(prontuarioAnexoRepository, never()).save(any());
        verify(storageService, never()).upload(anyString(), any(), anyString());
    }

    @Test
    void deveListarAnexosDoProntuario() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.RASCUNHO);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(prontuarioAnexoRepository.findByProntuarioIdAndOrganizacaoIdOrderByCriadoEmAsc(10L, ORGANIZACAO_ATUAL_ID))
                .thenReturn(java.util.List.of(anexo(1L, prontuario, "exame.pdf", "7/prontuarios/10/uuid.pdf")));

        var resultado = service.findAll(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNomeOriginal()).isEqualTo("exame.pdf");
    }

    @Test
    void deveBaixarAnexoAutorizado() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.RASCUNHO);
        ProntuarioAnexo anexo = anexo(1L, prontuario, "exame.pdf", "7/prontuarios/10/uuid.pdf");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(prontuarioAnexoRepository.findByIdAndProntuarioIdAndOrganizacaoId(1L, 10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(anexo));
        when(storageService.download("7/prontuarios/10/uuid.pdf")).thenReturn("conteúdo".getBytes());

        ProntuarioAnexoDownloadDTO resultado = service.download(10L, 1L);

        assertThat(resultado.getNomeOriginal()).isEqualTo("exame.pdf");
        assertThat(resultado.getTipoMime()).isEqualTo("application/pdf");
    }

    @Test
    void naoDevePermitirAcessoAAnexoDeOutraOrganizacao() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.RASCUNHO);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(prontuarioAnexoRepository.findByIdAndProntuarioIdAndOrganizacaoId(1L, 10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.download(10L, 1L))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(storageService, never()).download(anyString());
    }

    @Test
    void devePermitirExclusaoEmRascunho() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.RASCUNHO);
        ProntuarioAnexo anexo = anexo(1L, prontuario, "exame.pdf", "7/prontuarios/10/uuid.pdf");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(prontuarioAnexoRepository.findByIdAndProntuarioIdAndOrganizacaoId(1L, 10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(anexo));

        service.delete(10L, 1L);

        verify(prontuarioAnexoRepository).delete(anexo);
        verify(storageService).delete("7/prontuarios/10/uuid.pdf");
    }

    @Test
    void devePermitirExclusaoEmPendente() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.PENDENTE);
        ProntuarioAnexo anexo = anexo(1L, prontuario, "exame.pdf", "7/prontuarios/10/uuid.pdf");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(prontuarioAnexoRepository.findByIdAndProntuarioIdAndOrganizacaoId(1L, 10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(anexo));

        service.delete(10L, 1L);

        verify(prontuarioAnexoRepository).delete(anexo);
    }

    @Test
    void naoDevePermitirExclusaoEmAssinado() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.ASSINADO);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));

        assertThatThrownBy(() -> service.delete(10L, 1L))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(prontuarioAnexoRepository, never()).delete(any());
        verify(storageService, never()).delete(anyString());
    }

    @Test
    void naoDevePermitirArquivoVazio() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.RASCUNHO);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        MockMultipartFile vazio = new MockMultipartFile("arquivo", "exame.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> service.upload(10L, vazio))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(storageService, never()).upload(anyString(), any(), anyString());
    }

    @Test
    void naoDevePermitirArquivoAcimaDoLimite() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.RASCUNHO);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        byte[] grande = new byte[11 * 1024 * 1024];
        MockMultipartFile arquivoGrande = new MockMultipartFile("arquivo", "exame.pdf", "application/pdf", grande);

        assertThatThrownBy(() -> service.upload(10L, arquivoGrande))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(storageService, never()).upload(anyString(), any(), anyString());
    }

    @Test
    void naoDevePermitirTipoNaoPermitido() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.RASCUNHO);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        MockMultipartFile executavel = new MockMultipartFile("arquivo", "virus.exe", "application/x-msdownload", "conteudo".getBytes());

        assertThatThrownBy(() -> service.upload(10L, executavel))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(storageService, never()).upload(anyString(), any(), anyString());
    }

    @Test
    void deveRegistrarAuditoriaDeUpload() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.RASCUNHO);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(prontuarioAnexoRepository.save(any(ProntuarioAnexo.class))).thenAnswer(invocation -> {
            ProntuarioAnexo a = invocation.getArgument(0);
            a.setId(55L);
            return a;
        });

        service.upload(10L, arquivoValido());

        verify(auditoriaService).registrar(eq(EntidadeAuditavel.PRONTUARIO), eq(10L), eq(TipoEventoAuditoria.UPLOAD_ANEXO),
                eq(ORGANIZACAO_ATUAL_ID), eq(99L), eq("Usuário Logado"), eq(null), any(String.class));
    }

    @Test
    void deveRegistrarAuditoriaDeExclusao() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.RASCUNHO);
        ProntuarioAnexo anexo = anexo(1L, prontuario, "exame.pdf", "7/prontuarios/10/uuid.pdf");
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(prontuarioAnexoRepository.findByIdAndProntuarioIdAndOrganizacaoId(1L, 10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(anexo));

        service.delete(10L, 1L);

        verify(auditoriaService).registrar(eq(EntidadeAuditavel.PRONTUARIO), eq(10L), eq(TipoEventoAuditoria.EXCLUSAO_ANEXO),
                eq(ORGANIZACAO_ATUAL_ID), eq(99L), eq("Usuário Logado"), any(String.class), eq(null));
    }

    @Test
    void naoDeveCriarRegistroNoBancoQuandoStorageFalhaNoUpload() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.RASCUNHO);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        org.mockito.Mockito.doThrow(new StorageException("falha no storage", new RuntimeException()))
                .when(storageService).upload(anyString(), any(byte[].class), anyString());

        assertThatThrownBy(() -> service.upload(10L, arquivoValido())).isInstanceOf(StorageException.class);
        verify(prontuarioAnexoRepository, never()).save(any());
    }

    @Test
    void deveCompensarNoStorageQuandoBancoFalhaAposUpload() {
        Prontuario prontuario = prontuario(10L, StatusProntuario.RASCUNHO);
        when(prontuarioRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(prontuario));
        when(prontuarioAnexoRepository.save(any(ProntuarioAnexo.class))).thenThrow(new RuntimeException("falha no banco"));

        assertThatThrownBy(() -> service.upload(10L, arquivoValido())).isInstanceOf(RuntimeException.class);
        verify(storageService).upload(anyString(), any(byte[].class), anyString());
        verify(storageService).delete(anyString());
    }
}
