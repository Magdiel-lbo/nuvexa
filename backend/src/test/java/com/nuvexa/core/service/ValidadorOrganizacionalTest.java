package com.nuvexa.core.service;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.PapelOrganizacional;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.core.repository.VinculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Extraído de 7 services (paciente) e 6 services (profissional/autor/avaliador) que repetiam
 * esta mesma consulta byte-a-byte — ver auditoria de design patterns. Cobre isoladamente a regra
 * de filtro por organização que antes só era exercitada indiretamente através de cada service.
 */
class ValidadorOrganizacionalTest {

    private static final Long ORGANIZACAO_ATUAL_ID = 7L;

    private PacienteRepository pacienteRepository;
    private VinculoRepository vinculoRepository;
    private ValidadorOrganizacional validador;

    private Organizacao organizacaoAtual;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pacienteRepository = mock(PacienteRepository.class);
        vinculoRepository = mock(VinculoRepository.class);
        validador = new ValidadorOrganizacional(pacienteRepository, vinculoRepository);

        organizacaoAtual = organizacao(ORGANIZACAO_ATUAL_ID, "Clínica Atual");
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

    @Test
    void pacienteDaOrganizacaoDevolvePacienteQuandoPertenceAOrganizacao() {
        Paciente paciente = Paciente.builder()
                .organizacao(organizacaoAtual)
                .nome("Maria Souza")
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build();
        paciente.setId(1L);
        when(pacienteRepository.findByIdAndOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));

        assertThat(validador.pacienteDaOrganizacao(1L, ORGANIZACAO_ATUAL_ID)).contains(paciente);
    }

    @Test
    void pacienteDaOrganizacaoDevolveVazioQuandoNaoEncontrado() {
        when(pacienteRepository.findByIdAndOrganizacaoId(99L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.empty());

        assertThat(validador.pacienteDaOrganizacao(99L, ORGANIZACAO_ATUAL_ID)).isEmpty();
    }

    @Test
    void usuarioAtivoNaOrganizacaoDevolveUsuarioQuandoVinculoAtivoNaOrganizacaoInformada() {
        Usuario profissional = usuario(2L, "Joana Nutri");
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(2L))
                .thenReturn(List.of(vinculo(profissional, organizacaoAtual, true)));

        assertThat(validador.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).contains(profissional);
    }

    @Test
    void usuarioAtivoNaOrganizacaoDevolveVazioQuandoVinculoEDeOutraOrganizacao() {
        Organizacao outraOrganizacao = organizacao(8L, "Outra Clínica");
        Usuario profissionalDeOutraOrg = usuario(2L, "Carlos Nutri");
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(2L))
                .thenReturn(List.of(vinculo(profissionalDeOutraOrg, outraOrganizacao, true)));

        assertThat(validador.usuarioAtivoNaOrganizacao(2L, ORGANIZACAO_ATUAL_ID)).isEmpty();
    }

    @Test
    void usuarioAtivoNaOrganizacaoDevolveVazioQuandoSemVinculoNenhum() {
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(99L)).thenReturn(List.of());

        assertThat(validador.usuarioAtivoNaOrganizacao(99L, ORGANIZACAO_ATUAL_ID)).isEmpty();
    }
}
