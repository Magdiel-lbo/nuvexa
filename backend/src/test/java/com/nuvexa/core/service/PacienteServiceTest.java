package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.core.dto.request.PacienteUpdateRequestDTO;
import com.nuvexa.core.dto.response.PacienteResponseDTO;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.platform.exception.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Paciente (core) só cobre nome/dataNascimento/sexo — dado clínico de nutrição é
 * PerfilNutricionalServiceTest, na vertical.
 */
class PacienteServiceTest {

    private static final Long ORGANIZACAO_ATUAL_ID = 7L;

    private PacienteRepository pacienteRepository;
    private OrganizacaoScopedContext contexto;
    private ContextoDeAutenticacao contextoDeAutenticacao;
    private MessageSourceAccessor mensagens;
    private ValidadorOrganizacional validadorOrganizacional;

    private Organizacao organizacaoAtual;
    private PacienteService pacienteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pacienteRepository = mock(PacienteRepository.class);
        contexto = mock(OrganizacaoScopedContext.class);
        contextoDeAutenticacao = mock(ContextoDeAutenticacao.class);
        mensagens = mock(MessageSourceAccessor.class);
        validadorOrganizacional = mock(ValidadorOrganizacional.class);

        organizacaoAtual = Organizacao.builder()
                .nome("Clínica Atual")
                .tipo(TipoOrganizacao.CLINICA)
                .status(StatusOrganizacao.ATIVA)
                .build();
        organizacaoAtual.setId(ORGANIZACAO_ATUAL_ID);

        when(contexto.getContextoDeAutenticacao()).thenReturn(contextoDeAutenticacao);
        when(contexto.getMensagens()).thenReturn(mensagens);
        when(contextoDeAutenticacao.organizacaoAtual()).thenReturn(organizacaoAtual);
        when(contextoDeAutenticacao.organizacaoAtualId()).thenReturn(ORGANIZACAO_ATUAL_ID);
        when(mensagens.getMessage(any(String.class), any(Object[].class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> {
            Paciente paciente = invocation.getArgument(0);
            if (paciente.getId() == null) {
                paciente.setId(1L);
            }
            return paciente;
        });

        pacienteService = new PacienteService(pacienteRepository, new ModelMapper(), contexto, validadorOrganizacional);
    }

    private PacienteCreateRequestDTO createRequest() {
        return PacienteCreateRequestDTO.builder()
                .nome("Maria Souza")
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build();
    }

    @Test
    void deveCriarPacienteQuandoDadosValidos() {
        PacienteResponseDTO response = pacienteService.create(createRequest());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("Maria Souza");
        assertThat(response.getIdade()).isGreaterThan(0);
    }

    @Test
    void naoDeveCriarPacienteComDataNascimentoFutura() {
        PacienteCreateRequestDTO request = createRequest();
        request.setDataNascimento(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> pacienteService.create(request))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void deveAtualizarPacienteExistente() {
        Paciente existente = Paciente.builder()
                .organizacao(organizacaoAtual)
                .nome("Nome Antigo")
                .dataNascimento(LocalDate.of(1985, 1, 1))
                .sexo(Sexo.MASCULINO)
                .build();
        existente.setId(1L);
        when(validadorOrganizacional.pacienteDaOrganizacao(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(existente));

        PacienteUpdateRequestDTO request = PacienteUpdateRequestDTO.builder()
                .nome("Nome Novo")
                .dataNascimento(LocalDate.of(1985, 1, 1))
                .sexo(Sexo.MASCULINO)
                .build();

        PacienteResponseDTO response = pacienteService.update(1L, request);

        assertThat(response.getNome()).isEqualTo("Nome Novo");
    }

    @Test
    void deveFalharAoAtualizarPacienteInexistente() {
        when(validadorOrganizacional.pacienteDaOrganizacao(99L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.empty());

        PacienteUpdateRequestDTO request = PacienteUpdateRequestDTO.builder()
                .nome("Alguém")
                .dataNascimento(LocalDate.of(1990, 1, 1))
                .sexo(Sexo.MASCULINO)
                .build();

        assertThatThrownBy(() -> pacienteService.update(99L, request))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deveFalharAoBuscarPacienteInexistente() {
        when(validadorOrganizacional.pacienteDaOrganizacao(99L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.findById(99L))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
