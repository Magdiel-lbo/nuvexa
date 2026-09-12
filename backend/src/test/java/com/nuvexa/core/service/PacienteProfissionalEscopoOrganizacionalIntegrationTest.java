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
import com.nuvexa.core.repository.OrganizacaoRepository;
import com.nuvexa.core.repository.PacienteProfissionalRepository;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.core.repository.UsuarioRepository;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.platform.config.MessageConfig;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Mesmo isolamento multi-organização já provado para Paciente e Consulta, agora para o vínculo
 * Paciente-Profissional: paciente e profissional precisam ser ambos da organização atual.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MessageConfig.class, PacienteProfissionalService.class, ValidadorOrganizacional.class})
class PacienteProfissionalEscopoOrganizacionalIntegrationTest {

    @Autowired
    private PacienteProfissionalService pacienteProfissionalService;

    @Autowired
    private PacienteProfissionalRepository pacienteProfissionalRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VinculoRepository vinculoRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @MockitoBean
    private ContextoDeAutenticacao contextoDeAutenticacao;

    private Organizacao minhaOrganizacao;
    private Organizacao outraOrganizacao;
    private Paciente meuPaciente;
    private Usuario meuProfissional;

    @BeforeEach
    void setUp() {
        minhaOrganizacao = novaOrganizacao("Clínica A");
        outraOrganizacao = novaOrganizacao("Clínica B");
        meuPaciente = novoPaciente(minhaOrganizacao, "Ana da Minha Clinica");
        meuProfissional = novoProfissionalVinculado(minhaOrganizacao, "Joana Nutri");
        estarLogadoEm(minhaOrganizacao);
    }

    private Organizacao novaOrganizacao(String nome) {
        return organizacaoRepository.saveAndFlush(Organizacao.builder()
                .nome(nome)
                .tipo(TipoOrganizacao.CLINICA)
                .status(StatusOrganizacao.ATIVA)
                .build());
    }

    private void estarLogadoEm(Organizacao organizacao) {
        when(contextoDeAutenticacao.organizacaoAtual()).thenReturn(organizacao);
        when(contextoDeAutenticacao.organizacaoAtualId()).thenReturn(organizacao.getId());
    }

    private Paciente novoPaciente(Organizacao organizacao, String nome) {
        return pacienteRepository.saveAndFlush(Paciente.builder()
                .organizacao(organizacao)
                .nome(nome)
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build());
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

    private PacienteProfissionalCreateRequestDTO request(Long pacienteId, Long profissionalId) {
        return PacienteProfissionalCreateRequestDTO.builder()
                .pacienteId(pacienteId)
                .profissionalId(profissionalId)
                .build();
    }

    @Test
    void deveVincularPacienteEProfissionalDaMesmaOrganizacao() {
        PacienteProfissionalResponseDTO response = pacienteProfissionalService
                .vincular(request(meuPaciente.getId(), meuProfissional.getId()));

        PacienteProfissional persistido = pacienteProfissionalRepository.findById(response.getId()).orElseThrow();
        assertThat(persistido.getOrganizacao().getId()).isEqualTo(minhaOrganizacao.getId());
        assertThat(persistido.isAtivo()).isTrue();
    }

    @Test
    void naoDeveVincularPacienteDeOutraOrganizacao() {
        Paciente pacienteAlheio = novoPaciente(outraOrganizacao, "Bruno da Outra Clinica");

        assertThatThrownBy(() -> pacienteProfissionalService.vincular(request(pacienteAlheio.getId(), meuProfissional.getId())))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void naoDeveVincularProfissionalSemVinculoAtivoNaOrganizacaoAtual() {
        Usuario profissionalDeOutraOrg = novoProfissionalVinculado(outraOrganizacao, "Carlos Nutri");

        assertThatThrownBy(() -> pacienteProfissionalService.vincular(request(meuPaciente.getId(), profissionalDeOutraOrg.getId())))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void naoDeveVincularProfissionalInexistente() {
        assertThatThrownBy(() -> pacienteProfissionalService.vincular(request(meuPaciente.getId(), 999_999L)))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void naoDevePermitirVinculoAtivoDuplicado() {
        pacienteProfissionalService.vincular(request(meuPaciente.getId(), meuProfissional.getId()));

        assertThatThrownBy(() -> pacienteProfissionalService.vincular(request(meuPaciente.getId(), meuProfissional.getId())))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        assertThat(pacienteProfissionalRepository.findAll()).hasSize(1);
    }

    @Test
    void deveReativarVinculoInativoEmVezDeDuplicar() {
        PacienteProfissional inativo = pacienteProfissionalRepository.saveAndFlush(PacienteProfissional.builder()
                .paciente(meuPaciente)
                .profissional(meuProfissional)
                .organizacao(minhaOrganizacao)
                .ativo(false)
                .build());

        PacienteProfissionalResponseDTO response = pacienteProfissionalService
                .vincular(request(meuPaciente.getId(), meuProfissional.getId()));

        assertThat(response.getId()).isEqualTo(inativo.getId());
        assertThat(pacienteProfissionalRepository.findAll()).hasSize(1);
        assertThat(pacienteProfissionalRepository.findById(inativo.getId()).orElseThrow().isAtivo()).isTrue();
    }
}
