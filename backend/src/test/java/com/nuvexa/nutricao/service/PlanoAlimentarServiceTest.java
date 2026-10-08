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
import com.nuvexa.nutricao.dto.request.PlanoAlimentarCreateRequestDTO;
import com.nuvexa.nutricao.dto.request.PlanoAlimentarUpdateRequestDTO;
import com.nuvexa.nutricao.dto.response.PlanoAlimentarResponseDTO;
import com.nuvexa.nutricao.model.PlanoAlimentar;
import com.nuvexa.nutricao.model.StatusPlanoAlimentar;
import com.nuvexa.nutricao.repository.PlanoAlimentarRepository;
import com.nuvexa.platform.auditoria.AuditoriaService;
import com.nuvexa.platform.auditoria.EntidadeAuditavel;
import com.nuvexa.platform.auditoria.TipoEventoAuditoria;
import com.nuvexa.platform.exception.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cobre a máquina de estados de {@link StatusPlanoAlimentar} introduzida na Etapa 5 (M3):
 * ENCERRADO é terminal (bloqueia o update inteiro, mesmo padrão de ProntuarioService/
 * ConsultaService), e ATIVO -> RASCUNHO é a única transição proibida fora do caso terminal.
 * Primeiro teste deste service no projeto (nunca teve cobertura antes desta etapa).
 */
class PlanoAlimentarServiceTest {

    private static final Long ORGANIZACAO_ATUAL_ID = 7L;

    private PlanoAlimentarRepository planoAlimentarRepository;
    private ValidadorOrganizacional validadorOrganizacional;
    private ModelMapper modelMapper;
    private OrganizacaoScopedContext contexto;
    private ContextoDeAutenticacao contextoDeAutenticacao;
    private MessageSourceAccessor mensagens;
    private AuditoriaService auditoriaService;

    private Organizacao organizacaoAtual;
    private PlanoAlimentarService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        planoAlimentarRepository = mock(PlanoAlimentarRepository.class);
        validadorOrganizacional = mock(ValidadorOrganizacional.class);
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        contexto = mock(OrganizacaoScopedContext.class);
        contextoDeAutenticacao = mock(ContextoDeAutenticacao.class);
        mensagens = mock(MessageSourceAccessor.class);
        auditoriaService = mock(AuditoriaService.class);

        organizacaoAtual = Organizacao.builder().nome("Clínica Atual").tipo(TipoOrganizacao.CLINICA).status(StatusOrganizacao.ATIVA).build();
        organizacaoAtual.setId(ORGANIZACAO_ATUAL_ID);

        when(contexto.getContextoDeAutenticacao()).thenReturn(contextoDeAutenticacao);
        when(contexto.getMensagens()).thenReturn(mensagens);
        when(contextoDeAutenticacao.organizacaoAtualId()).thenReturn(ORGANIZACAO_ATUAL_ID);
        when(contextoDeAutenticacao.usuarioAtual()).thenReturn(usuario(99L, "Usuário Logado"));
        when(mensagens.getMessage(any(String.class), any(Object[].class))).thenAnswer(invocation -> invocation.getArgument(0));

        service = new PlanoAlimentarService(planoAlimentarRepository, modelMapper, contexto, auditoriaService, validadorOrganizacional);
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

    private PlanoAlimentar plano(Long id, StatusPlanoAlimentar status) {
        PlanoAlimentar plano = PlanoAlimentar.builder()
                .organizacao(organizacaoAtual)
                .paciente(paciente(1L))
                .autor(usuario(2L, "Joana Nutri"))
                .nome("Plano Base")
                .dataInicio(LocalDate.now())
                .calorias(2000)
                .refeicoesPorDia(5)
                .status(status)
                .build();
        plano.setId(id);
        return plano;
    }

    private PlanoAlimentarUpdateRequestDTO updateRequest(StatusPlanoAlimentar status) {
        return PlanoAlimentarUpdateRequestDTO.builder()
                .autorId(2L)
                .nome("Plano Atualizado")
                .dataInicio(LocalDate.now())
                .calorias(2200)
                .refeicoesPorDia(4)
                .status(status)
                .build();
    }

    private PlanoAlimentarCreateRequestDTO createRequest(StatusPlanoAlimentar status) {
        return PlanoAlimentarCreateRequestDTO.builder()
                .pacienteId(1L)
                .autorId(2L)
                .nome("Plano Base")
                .dataInicio(LocalDate.now())
                .calorias(2000)
                .refeicoesPorDia(5)
                .status(status)
                .build();
    }

    @Test
    void naoDevePermitirEditarPlanoEncerrado() {
        PlanoAlimentar existente = plano(10L, StatusPlanoAlimentar.ENCERRADO);
        when(planoAlimentarRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.update(10L, updateRequest(StatusPlanoAlimentar.ENCERRADO)))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(planoAlimentarRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirVoltarPlanoAtivoParaRascunho() {
        PlanoAlimentar existente = plano(10L, StatusPlanoAlimentar.ATIVO);
        when(planoAlimentarRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.update(10L, updateRequest(StatusPlanoAlimentar.RASCUNHO)))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(planoAlimentarRepository, never()).save(any());
    }

    @Test
    void devePermitirEncerrarPlanoAtivo() {
        PlanoAlimentar existente = plano(10L, StatusPlanoAlimentar.ATIVO);
        when(planoAlimentarRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(usuario(2L, "Joana Nutri")));
        when(planoAlimentarRepository.save(any(PlanoAlimentar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanoAlimentarResponseDTO atualizado = service.update(10L, updateRequest(StatusPlanoAlimentar.ENCERRADO));

        assertThat(atualizado.getStatus()).isEqualTo(StatusPlanoAlimentar.ENCERRADO);
    }

    @Test
    void devePermitirAtivarPlanoRascunho() {
        PlanoAlimentar existente = plano(10L, StatusPlanoAlimentar.RASCUNHO);
        when(planoAlimentarRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(usuario(2L, "Joana Nutri")));
        when(planoAlimentarRepository.save(any(PlanoAlimentar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanoAlimentarResponseDTO atualizado = service.update(10L, updateRequest(StatusPlanoAlimentar.ATIVO));

        assertThat(atualizado.getStatus()).isEqualTo(StatusPlanoAlimentar.ATIVO);
    }

    @Test
    void devePermitirEncerrarPlanoRascunhoDiretamente() {
        PlanoAlimentar existente = plano(10L, StatusPlanoAlimentar.RASCUNHO);
        when(planoAlimentarRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(usuario(2L, "Joana Nutri")));
        when(planoAlimentarRepository.save(any(PlanoAlimentar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanoAlimentarResponseDTO atualizado = service.update(10L, updateRequest(StatusPlanoAlimentar.ENCERRADO));

        assertThat(atualizado.getStatus()).isEqualTo(StatusPlanoAlimentar.ENCERRADO);
    }

    @Test
    void naoDevePermitirExcluirPlanoEncerrado() {
        PlanoAlimentar existente = plano(10L, StatusPlanoAlimentar.ENCERRADO);
        when(planoAlimentarRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.delete(10L))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(planoAlimentarRepository, never()).delete(any());
        verify(auditoriaService, never()).registrar(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void devePermitirExcluirPlanoAtivo() {
        PlanoAlimentar existente = plano(10L, StatusPlanoAlimentar.ATIVO);
        when(planoAlimentarRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        service.delete(10L);

        verify(planoAlimentarRepository).delete(existente);
    }

    @Test
    void deveRegistrarEventoCriacaoNaAuditoria() {
        Paciente paciente = paciente(1L);
        Usuario autor = usuario(2L, "Joana Nutri");
        when(validadorOrganizacional.pacienteDaOrganizacao(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(autor));
        when(planoAlimentarRepository.save(any(PlanoAlimentar.class))).thenAnswer(invocation -> {
            PlanoAlimentar planoAlimentar = invocation.getArgument(0);
            planoAlimentar.setId(10L);
            return planoAlimentar;
        });

        service.create(createRequest(StatusPlanoAlimentar.RASCUNHO));

        verify(auditoriaService).registrar(eq(EntidadeAuditavel.PLANO_ALIMENTAR), eq(10L), eq(TipoEventoAuditoria.CRIACAO),
                eq(ORGANIZACAO_ATUAL_ID), eq(99L), eq("Usuário Logado"), isNull(), any(String.class));
    }

    @Test
    void deveRegistrarEventoEdicaoNaAuditoria() {
        PlanoAlimentar existente = plano(10L, StatusPlanoAlimentar.RASCUNHO);
        when(planoAlimentarRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(usuario(2L, "Joana Nutri")));
        when(planoAlimentarRepository.save(any(PlanoAlimentar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(10L, updateRequest(StatusPlanoAlimentar.ATIVO));

        verify(auditoriaService).registrar(eq(EntidadeAuditavel.PLANO_ALIMENTAR), eq(10L), eq(TipoEventoAuditoria.EDICAO),
                eq(ORGANIZACAO_ATUAL_ID), eq(99L), eq("Usuário Logado"), any(String.class), any(String.class));
    }

    @Test
    void deveRegistrarEncerramentoComoEdicaoNaAuditoria() {
        PlanoAlimentar existente = plano(10L, StatusPlanoAlimentar.ATIVO);
        when(planoAlimentarRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));
        when(validadorOrganizacional.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(usuario(2L, "Joana Nutri")));
        when(planoAlimentarRepository.save(any(PlanoAlimentar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(10L, updateRequest(StatusPlanoAlimentar.ENCERRADO));

        verify(auditoriaService).registrar(eq(EntidadeAuditavel.PLANO_ALIMENTAR), eq(10L), eq(TipoEventoAuditoria.EDICAO),
                eq(ORGANIZACAO_ATUAL_ID), eq(99L), eq("Usuário Logado"),
                argThat(antes -> antes.contains("status=ATIVO")), argThat(depois -> depois.contains("status=ENCERRADO")));
    }

    @Test
    void deveRegistrarEventoExclusaoNaAuditoria() {
        PlanoAlimentar existente = plano(10L, StatusPlanoAlimentar.RASCUNHO);
        when(planoAlimentarRepository.findByIdAndOrganizacaoId(10L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        service.delete(10L);

        verify(auditoriaService).registrar(eq(EntidadeAuditavel.PLANO_ALIMENTAR), eq(10L), eq(TipoEventoAuditoria.EXCLUSAO),
                eq(ORGANIZACAO_ATUAL_ID), eq(99L), eq("Usuário Logado"), any(String.class), isNull());
    }
}
