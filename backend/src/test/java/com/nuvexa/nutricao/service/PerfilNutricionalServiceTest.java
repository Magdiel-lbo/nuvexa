package com.nuvexa.nutricao.service;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.core.service.ContextoDeAutenticacao;
import com.nuvexa.core.service.OrganizacaoScopedContext;
import com.nuvexa.nutricao.calculator.GastoCaloricoCalculator;
import com.nuvexa.nutricao.calculator.ImcCalculator;
import com.nuvexa.nutricao.calculator.TaxaMetabolicaCalculator;
import com.nuvexa.nutricao.dto.request.PerfilNutricionalCreateRequestDTO;
import com.nuvexa.nutricao.dto.request.PerfilNutricionalUpdateRequestDTO;
import com.nuvexa.nutricao.dto.response.PerfilNutricionalResponseDTO;
import com.nuvexa.nutricao.model.Avaliacao;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.nutricao.model.PerfilNutricional;
import com.nuvexa.nutricao.model.StatusAvaliacao;
import com.nuvexa.nutricao.model.TipoAvaliacao;
import com.nuvexa.nutricao.repository.AvaliacaoRepository;
import com.nuvexa.nutricao.repository.PerfilNutricionalRepository;
import com.nuvexa.platform.exception.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cobre a decisão do Passo 1 (peso deixa de ser estado do perfil, vira Avaliacao inicial na
 * criação). Unit test puro (sem Spring context) — mesmo padrão de PacienteProfissionalServiceTest.
 */
class PerfilNutricionalServiceTest {

    private static final Long ORGANIZACAO_ATUAL_ID = 7L;

    private PerfilNutricionalRepository perfilNutricionalRepository;
    private PacienteRepository pacienteRepository;
    private AvaliacaoRepository avaliacaoRepository;
    private AvaliacaoService avaliacaoService;
    private ModelMapper modelMapper;
    private ImcCalculator imcCalculator;
    private TaxaMetabolicaCalculator taxaMetabolicaCalculator;
    private GastoCaloricoCalculator gastoCaloricoCalculator;
    private OrganizacaoScopedContext contexto;
    private ContextoDeAutenticacao contextoDeAutenticacao;
    private MessageSourceAccessor mensagens;

    private Organizacao organizacaoAtual;
    private Usuario usuarioLogado;
    private PerfilNutricionalService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        perfilNutricionalRepository = mock(PerfilNutricionalRepository.class);
        pacienteRepository = mock(PacienteRepository.class);
        avaliacaoRepository = mock(AvaliacaoRepository.class);
        avaliacaoService = mock(AvaliacaoService.class);
        modelMapper = mock(ModelMapper.class);
        imcCalculator = mock(ImcCalculator.class);
        taxaMetabolicaCalculator = mock(TaxaMetabolicaCalculator.class);
        gastoCaloricoCalculator = mock(GastoCaloricoCalculator.class);
        contexto = mock(OrganizacaoScopedContext.class);
        contextoDeAutenticacao = mock(ContextoDeAutenticacao.class);
        mensagens = mock(MessageSourceAccessor.class);

        organizacaoAtual = organizacao(ORGANIZACAO_ATUAL_ID, "Clínica Atual");
        usuarioLogado = usuario(3L, "Joana Nutri");

        when(contexto.getContextoDeAutenticacao()).thenReturn(contextoDeAutenticacao);
        when(contexto.getMensagens()).thenReturn(mensagens);
        when(contextoDeAutenticacao.organizacaoAtual()).thenReturn(organizacaoAtual);
        when(contextoDeAutenticacao.organizacaoAtualId()).thenReturn(ORGANIZACAO_ATUAL_ID);
        when(contextoDeAutenticacao.usuarioAtual()).thenReturn(usuarioLogado);
        when(mensagens.getMessage(any(String.class), any(Object[].class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(perfilNutricionalRepository.save(any(PerfilNutricional.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service = new PerfilNutricionalService(
                perfilNutricionalRepository, pacienteRepository, avaliacaoRepository, avaliacaoService,
                modelMapper, imcCalculator, taxaMetabolicaCalculator, gastoCaloricoCalculator, contexto);
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

    private PerfilNutricional perfil(Paciente paciente, BigDecimal caloriasDiariasManuais) {
        return PerfilNutricional.builder()
                .paciente(paciente)
                .altura(new BigDecimal("1.70"))
                .objetivo(Objetivo.MANUTENCAO_PESO)
                .nivelAtividade(NivelAtividade.SEDENTARIO)
                .caloriasDiariasManuais(caloriasDiariasManuais)
                .build();
    }

    private PerfilNutricionalCreateRequestDTO createRequest(BigDecimal pesoInicial) {
        return PerfilNutricionalCreateRequestDTO.builder()
                .altura(new BigDecimal("1.70"))
                .pesoInicial(pesoInicial)
                .objetivo(Objetivo.MANUTENCAO_PESO)
                .nivelAtividade(NivelAtividade.SEDENTARIO)
                .build();
    }

    @Test
    void deveCriarPerfilEAvaliacaoInicialNaMesmaOperacao() {
        Paciente paciente = paciente(1L);
        when(pacienteRepository.findByIdAndOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));
        when(avaliacaoService.buscarUltimaAvaliacaoComPeso(1L)).thenReturn(Optional.empty());

        service.create(1L, createRequest(new BigDecimal("58.00")));

        ArgumentCaptor<Avaliacao> avaliacaoCaptor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(avaliacaoRepository).save(avaliacaoCaptor.capture());
        Avaliacao avaliacaoSalva = avaliacaoCaptor.getValue();

        assertThat(avaliacaoSalva.getPaciente()).isEqualTo(paciente);
        assertThat(avaliacaoSalva.getAvaliador()).isEqualTo(usuarioLogado);
        assertThat(avaliacaoSalva.getOrganizacao()).isEqualTo(organizacaoAtual);
        assertThat(avaliacaoSalva.getData()).isEqualTo(LocalDate.now());
        assertThat(avaliacaoSalva.getTipo()).isEqualTo(TipoAvaliacao.ANTROPOMETRIA);
        assertThat(avaliacaoSalva.getStatus()).isEqualTo(StatusAvaliacao.CONCLUIDA);
        assertThat(avaliacaoSalva.getPeso()).isEqualByComparingTo("58.00");

        ArgumentCaptor<PerfilNutricional> perfilCaptor = ArgumentCaptor.forClass(PerfilNutricional.class);
        verify(perfilNutricionalRepository).save(perfilCaptor.capture());
        assertThat(perfilCaptor.getValue().getPeso()).isNull();
    }

    @Test
    void deveFalharAoCriarComPesoInicialInvalido() {
        Paciente paciente = paciente(1L);
        when(pacienteRepository.findByIdAndOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));

        assertThatThrownBy(() -> service.create(1L, createRequest(BigDecimal.ZERO)))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(perfilNutricionalRepository, never()).save(any());
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void updateNaoDeveCriarNemAlterarAvaliacao() {
        Paciente paciente = paciente(1L);
        PerfilNutricional perfilExistente = perfil(paciente, null);
        when(perfilNutricionalRepository.findByPacienteIdAndPacienteOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID))
                .thenReturn(Optional.of(perfilExistente));
        when(avaliacaoService.buscarUltimaAvaliacaoComPeso(1L)).thenReturn(Optional.empty());

        PerfilNutricionalUpdateRequestDTO request = PerfilNutricionalUpdateRequestDTO.builder()
                .altura(new BigDecimal("1.71"))
                .objetivo(Objetivo.GANHO_MASSA_MUSCULAR)
                .nivelAtividade(NivelAtividade.MODERADAMENTE_ATIVO)
                .build();

        service.update(1L, request);

        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void deveRetornarPesoECalculosNulosQuandoNaoHaAvaliacao() {
        Paciente paciente = paciente(1L);
        PerfilNutricional perfil = perfil(paciente, null);
        when(perfilNutricionalRepository.findByPacienteIdAndPacienteOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(perfil));
        when(avaliacaoService.buscarUltimaAvaliacaoComPeso(1L)).thenReturn(Optional.empty());

        PerfilNutricionalResponseDTO response = service.findByPacienteId(1L);

        assertThat(response.getPeso()).isNull();
        assertThat(response.getAvaliacaoAtualId()).isNull();
        assertThat(response.getImc()).isNull();
        assertThat(response.getTaxaMetabolicaBasal()).isNull();
        assertThat(response.getGastoCaloricoDiario()).isNull();
    }

    @Test
    void deveUsarCaloriasManuaisMesmoSemAvaliacao() {
        Paciente paciente = paciente(1L);
        PerfilNutricional perfil = perfil(paciente, new BigDecimal("2000"));
        when(perfilNutricionalRepository.findByPacienteIdAndPacienteOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(perfil));
        when(avaliacaoService.buscarUltimaAvaliacaoComPeso(1L)).thenReturn(Optional.empty());

        PerfilNutricionalResponseDTO response = service.findByPacienteId(1L);

        assertThat(response.getGastoCaloricoDiario()).isEqualByComparingTo("2000");
        assertThat(response.getImc()).isNull();
    }

    @Test
    void deveCalcularComPesoDaAvaliacaoMaisRecente() {
        Paciente paciente = paciente(1L);
        PerfilNutricional perfil = perfil(paciente, null);
        Avaliacao ultimaAvaliacao = Avaliacao.builder()
                .paciente(paciente)
                .data(LocalDate.now())
                .tipo(TipoAvaliacao.ANTROPOMETRIA)
                .status(StatusAvaliacao.CONCLUIDA)
                .peso(new BigDecimal("70.00"))
                .build();
        ultimaAvaliacao.setId(42L);

        when(perfilNutricionalRepository.findByPacienteIdAndPacienteOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(perfil));
        when(avaliacaoService.buscarUltimaAvaliacaoComPeso(1L)).thenReturn(Optional.of(ultimaAvaliacao));
        when(imcCalculator.calculate(any(), any())).thenReturn(new BigDecimal("24.2"));
        when(imcCalculator.classify(any())).thenReturn("Normal");
        when(taxaMetabolicaCalculator.calculate(any(), any(), anyInt(), any())).thenReturn(new BigDecimal("1500"));
        when(gastoCaloricoCalculator.calculate(any(), any())).thenReturn(new BigDecimal("1800"));

        PerfilNutricionalResponseDTO response = service.findByPacienteId(1L);

        assertThat(response.getPeso()).isEqualByComparingTo("70.00");
        assertThat(response.getAvaliacaoAtualId()).isEqualTo(42L);
        assertThat(response.getImc()).isEqualByComparingTo("24.2");
        assertThat(response.getGastoCaloricoDiario()).isEqualByComparingTo("1800");
    }
}
