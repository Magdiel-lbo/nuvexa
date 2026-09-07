package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.PacienteProfissionalCreateRequestDTO;
import com.nuvexa.core.dto.response.PacienteProfissionalResponseDTO;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.PacienteProfissional;
import com.nuvexa.core.model.PapelOrganizacional;
import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.PacienteProfissionalRepository;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.platform.exception.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PacienteProfissionalServiceTest {

    private static final Long ORGANIZACAO_ATUAL_ID = 7L;

    @Mock
    private PacienteProfissionalRepository pacienteProfissionalRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private VinculoRepository vinculoRepository;

    @Mock
    private MessageSourceAccessor mensagens;

    @Mock
    private ContextoDeAutenticacao contextoDeAutenticacao;

    private Organizacao organizacaoAtual;

    private PacienteProfissionalService pacienteProfissionalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        organizacaoAtual = organizacao(ORGANIZACAO_ATUAL_ID, "Clínica Atual");
        when(contextoDeAutenticacao.organizacaoAtual()).thenReturn(organizacaoAtual);
        when(contextoDeAutenticacao.organizacaoAtualId()).thenReturn(ORGANIZACAO_ATUAL_ID);
        when(mensagens.getMessage(any(String.class), any(Object[].class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pacienteProfissionalRepository.save(any(PacienteProfissional.class))).thenAnswer(invocation -> {
            PacienteProfissional vinculo = invocation.getArgument(0);
            if (vinculo.getId() == null) {
                vinculo.setId(1L);
            }
            return vinculo;
        });
        pacienteProfissionalService = new PacienteProfissionalService(
                pacienteProfissionalRepository, pacienteRepository, vinculoRepository, contextoDeAutenticacao, mensagens);
    }

    private Organizacao organizacao(Long id, String nome) {
        Organizacao organizacao = Organizacao.builder()
                .nome(nome)
                .tipo(TipoOrganizacao.CLINICA)
                .status(StatusOrganizacao.ATIVA)
                .build();
        organizacao.setId(id);
        return organizacao;
    }

    private Paciente paciente(Long id, Organizacao organizacao) {
        Paciente paciente = Paciente.builder()
                .organizacao(organizacao)
                .nome("Maria Souza")
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build();
        paciente.setId(id);
        return paciente;
    }

    private Usuario usuario(Long id, String nome) {
        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(nome.toLowerCase().replace(" ", ".") + "@nuvexa.com")
                .senha("hash")
                .perfil(Perfil.PROFISSIONAL)
                .ativo(true)
                .build();
        usuario.setId(id);
        return usuario;
    }

    private Vinculo vinculo(Usuario usuario, Organizacao organizacao, boolean ativo) {
        return Vinculo.builder()
                .usuario(usuario)
                .organizacao(organizacao)
                .papel(PapelOrganizacional.MEMBRO)
                .ativo(ativo)
                .build();
    }

    private PacienteProfissionalCreateRequestDTO request(Long pacienteId, Long profissionalId) {
        return PacienteProfissionalCreateRequestDTO.builder()
                .pacienteId(pacienteId)
                .profissionalId(profissionalId)
                .build();
    }

    @Test
    void deveVincularProfissionalDaOrganizacaoAtualAoPaciente() {
        Paciente paciente = paciente(1L, organizacaoAtual);
        Usuario profissional = usuario(2L, "Joana Nutri");
        when(pacienteRepository.findByIdAndOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(2L))
                .thenReturn(List.of(vinculo(profissional, organizacaoAtual, true)));
        when(pacienteProfissionalRepository.findByPacienteIdAndProfissionalId(1L, 2L)).thenReturn(Optional.empty());

        PacienteProfissionalResponseDTO response = pacienteProfissionalService.vincular(request(1L, 2L));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPacienteId()).isEqualTo(1L);
        assertThat(response.getProfissionalId()).isEqualTo(2L);
        assertThat(response.isAtivo()).isTrue();
    }

    @Test
    void deveFalharQuandoPacienteNaoPertenceAOrganizacaoAtual() {
        when(pacienteRepository.findByIdAndOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteProfissionalService.vincular(request(1L, 2L)))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(pacienteProfissionalRepository, never()).save(any());
    }

    @Test
    void deveFalharQuandoProfissionalNaoTemVinculoAtivoNaOrganizacaoAtual() {
        Paciente paciente = paciente(1L, organizacaoAtual);
        Organizacao outraOrganizacao = organizacao(8L, "Outra Clínica");
        Usuario profissionalDeOutraOrg = usuario(2L, "Carlos Nutri");
        when(pacienteRepository.findByIdAndOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(2L))
                .thenReturn(List.of(vinculo(profissionalDeOutraOrg, outraOrganizacao, true)));

        assertThatThrownBy(() -> pacienteProfissionalService.vincular(request(1L, 2L)))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(pacienteProfissionalRepository, never()).save(any());
    }

    @Test
    void deveFalharQuandoProfissionalInexistente() {
        Paciente paciente = paciente(1L, organizacaoAtual);
        when(pacienteRepository.findByIdAndOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(99L)).thenReturn(List.of());

        assertThatThrownBy(() -> pacienteProfissionalService.vincular(request(1L, 99L)))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void deveFalharQuandoVinculoAtivoJaExiste() {
        Paciente paciente = paciente(1L, organizacaoAtual);
        Usuario profissional = usuario(2L, "Joana Nutri");
        PacienteProfissional vinculoExistente = PacienteProfissional.builder()
                .paciente(paciente).profissional(profissional).organizacao(organizacaoAtual).ativo(true).build();
        vinculoExistente.setId(5L);

        when(pacienteRepository.findByIdAndOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(2L))
                .thenReturn(List.of(vinculo(profissional, organizacaoAtual, true)));
        when(pacienteProfissionalRepository.findByPacienteIdAndProfissionalId(1L, 2L)).thenReturn(Optional.of(vinculoExistente));

        assertThatThrownBy(() -> pacienteProfissionalService.vincular(request(1L, 2L)))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(pacienteProfissionalRepository, never()).save(any());
    }

    @Test
    void deveReativarVinculoInativoEmVezDeCriarOutro() {
        Paciente paciente = paciente(1L, organizacaoAtual);
        Usuario profissional = usuario(2L, "Joana Nutri");
        PacienteProfissional vinculoInativo = PacienteProfissional.builder()
                .paciente(paciente).profissional(profissional).organizacao(organizacaoAtual).ativo(false).build();
        vinculoInativo.setId(5L);

        when(pacienteRepository.findByIdAndOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(2L))
                .thenReturn(List.of(vinculo(profissional, organizacaoAtual, true)));
        when(pacienteProfissionalRepository.findByPacienteIdAndProfissionalId(1L, 2L)).thenReturn(Optional.of(vinculoInativo));

        PacienteProfissionalResponseDTO response = pacienteProfissionalService.vincular(request(1L, 2L));

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.isAtivo()).isTrue();
        assertThat(vinculoInativo.isAtivo()).isTrue();
    }
}
