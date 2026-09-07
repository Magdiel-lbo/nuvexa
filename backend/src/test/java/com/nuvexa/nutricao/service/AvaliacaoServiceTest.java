package com.nuvexa.nutricao.service;

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
import com.nuvexa.core.service.ContextoDeAutenticacao;
import com.nuvexa.core.service.OrganizacaoScopedContext;
import com.nuvexa.nutricao.dto.request.AvaliacaoCreateRequestDTO;
import com.nuvexa.nutricao.model.Avaliacao;
import com.nuvexa.nutricao.model.StatusAvaliacao;
import com.nuvexa.nutricao.model.TipoAvaliacao;
import com.nuvexa.nutricao.repository.AvaliacaoRepository;
import com.nuvexa.platform.exception.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
    private PacienteRepository pacienteRepository;
    private VinculoRepository vinculoRepository;
    private ModelMapper modelMapper;
    private OrganizacaoScopedContext contexto;
    private ContextoDeAutenticacao contextoDeAutenticacao;
    private MessageSourceAccessor mensagens;

    private Organizacao organizacaoAtual;
    private AvaliacaoService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        avaliacaoRepository = mock(AvaliacaoRepository.class);
        pacienteRepository = mock(PacienteRepository.class);
        vinculoRepository = mock(VinculoRepository.class);
        modelMapper = mock(ModelMapper.class);
        contexto = mock(OrganizacaoScopedContext.class);
        contextoDeAutenticacao = mock(ContextoDeAutenticacao.class);
        mensagens = mock(MessageSourceAccessor.class);

        organizacaoAtual = organizacao(ORGANIZACAO_ATUAL_ID, "Clínica Atual");

        when(contexto.getContextoDeAutenticacao()).thenReturn(contextoDeAutenticacao);
        when(contexto.getMensagens()).thenReturn(mensagens);
        when(contextoDeAutenticacao.organizacaoAtual()).thenReturn(organizacaoAtual);
        when(contextoDeAutenticacao.organizacaoAtualId()).thenReturn(ORGANIZACAO_ATUAL_ID);
        when(mensagens.getMessage(any(String.class), any(Object[].class))).thenAnswer(invocation -> invocation.getArgument(0));

        service = new AvaliacaoService(avaliacaoRepository, pacienteRepository, vinculoRepository, modelMapper, contexto);
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

    private Vinculo vinculo(Usuario usuario, Organizacao organizacao, boolean ativo) {
        return Vinculo.builder().usuario(usuario).organizacao(organizacao).papel(PapelOrganizacional.MEMBRO).ativo(ativo).build();
    }

    @Test
    void naoDeveCriarAvaliacaoComDataFuturaEPesoPreenchido() {
        Paciente paciente = paciente(1L);
        Usuario avaliador = usuario(2L, "Joana Nutri");
        when(pacienteRepository.findByIdAndOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(2L)).thenReturn(List.of(vinculo(avaliador, organizacaoAtual, true)));

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
        when(pacienteRepository.findByIdAndOrganizacaoId(1L, ORGANIZACAO_ATUAL_ID)).thenReturn(Optional.of(paciente));
        when(vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(2L)).thenReturn(List.of(vinculo(avaliador, organizacaoAtual, true)));
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
}
