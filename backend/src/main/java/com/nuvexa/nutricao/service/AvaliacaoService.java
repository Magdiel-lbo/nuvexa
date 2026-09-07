package com.nuvexa.nutricao.service;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.core.service.OrganizacaoScopedContext;
import com.nuvexa.nutricao.dto.request.AvaliacaoCreateRequestDTO;
import com.nuvexa.nutricao.dto.request.AvaliacaoUpdateRequestDTO;
import com.nuvexa.nutricao.dto.response.AvaliacaoEnumsResponseDTO;
import com.nuvexa.nutricao.dto.response.AvaliacaoResponseDTO;
import com.nuvexa.nutricao.model.Avaliacao;
import com.nuvexa.nutricao.model.QAvaliacao;
import com.nuvexa.nutricao.model.StatusAvaliacao;
import com.nuvexa.nutricao.model.TipoAvaliacao;
import com.nuvexa.nutricao.repository.AvaliacaoRepository;
import com.nuvexa.platform.exception.NegocioException;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final PacienteRepository pacienteRepository;
    private final VinculoRepository vinculoRepository;
    private final ModelMapper modelMapper;
    private final OrganizacaoScopedContext contexto;

    public AvaliacaoResponseDTO create(AvaliacaoCreateRequestDTO request) {
        Paciente paciente = buscarPacienteOuFalhar(request.getPacienteId());
        Usuario avaliador = buscarAvaliadorOuFalhar(request.getAvaliadorId());
        validarDataNaoFutura(request.getData(), request.getPeso());

        Avaliacao avaliacao = avaliacaoRepository.save(
                request.toAvaliacao(contexto.getContextoDeAutenticacao().organizacaoAtual(), paciente, avaliador));
        log.info("Avaliação criada com id={}", avaliacao.getId());
        return AvaliacaoResponseDTO.from(avaliacao, calcularVariacao(avaliacao));
    }

    public AvaliacaoResponseDTO update(Long id, AvaliacaoUpdateRequestDTO request) {
        Avaliacao avaliacao = buscarAvaliacaoOuFalhar(id);
        Usuario avaliador = buscarAvaliadorOuFalhar(request.getAvaliadorId());
        validarDataNaoFutura(request.getData(), request.getPeso());
        request.atualizar(avaliacao, avaliador, modelMapper);

        Avaliacao saved = avaliacaoRepository.save(avaliacao);
        log.info("Avaliação atualizada com id={}", id);
        return AvaliacaoResponseDTO.from(saved, calcularVariacao(saved));
    }

    public AvaliacaoResponseDTO findById(Long id) {
        Avaliacao avaliacao = buscarAvaliacaoOuFalhar(id);
        return AvaliacaoResponseDTO.from(avaliacao, calcularVariacao(avaliacao));
    }

    public List<AvaliacaoResponseDTO> findAll(Long pacienteId, String busca) {
        return comVariacao(buscarAvaliacoes(pacienteId, normalizarBusca(busca)));
    }

    public AvaliacaoEnumsResponseDTO getEnums() {
        return AvaliacaoEnumsResponseDTO.of();
    }

    public void delete(Long id) {
        Avaliacao avaliacao = buscarAvaliacaoOuFalhar(id);
        avaliacaoRepository.delete(avaliacao);
        log.info("Avaliação removida com id={}", id);
    }

    /**
     * Fábrica da avaliação antropométrica rápida criada implicitamente (perfil nutricional na
     * criação, e futuramente Consulta ao ir para REALIZADA) — não salva, quem chama decide o
     * repository e a transação. Estático porque não depende de nenhum colaborador do service,
     * mesmo padrão de {@link #calcularVariacoesPorPaciente}.
     */
    public static Avaliacao criarMedidaRapida(
            Organizacao organizacao, Paciente paciente, Usuario avaliador, LocalDate data, BigDecimal peso) {
        return Avaliacao.builder()
                .organizacao(organizacao)
                .paciente(paciente)
                .avaliador(avaliador)
                .data(data)
                .tipo(TipoAvaliacao.ANTROPOMETRIA)
                .status(StatusAvaliacao.CONCLUIDA)
                .peso(peso)
                .percentualGordura(null)
                .build();
    }

    /**
     * Peso "atual" do paciente = avaliação mais recente com peso preenchido (exclui AGENDADA, que
     * ainda não tem medida). Desempate por id quando duas avaliações caem na mesma data — id maior
     * é a inserida por último. Filtra data futura como defesa em profundidade: a validação de
     * escrita em {@link #create}/{@link #update} já impede uma avaliação CONCLUIDA com peso ser
     * datada no futuro, então este filtro não deveria excluir nada em operação normal.
     */
    public Optional<Avaliacao> buscarUltimaAvaliacaoComPeso(Long pacienteId) {
        QAvaliacao a = QAvaliacao.avaliacao;
        Avaliacao maisRecente = contexto.getQueryFactory()
                .selectFrom(a)
                .where(a.organizacao.id.eq(contexto.getContextoDeAutenticacao().organizacaoAtualId())
                        .and(a.paciente.id.eq(pacienteId))
                        .and(a.peso.isNotNull())
                        .and(a.data.loe(LocalDate.now())))
                .orderBy(a.data.desc(), a.id.desc())
                .fetchFirst();
        return Optional.ofNullable(maisRecente);
    }

    /**
     * Mesmo critério de {@link #buscarUltimaAvaliacaoComPeso}, mas em lote — usado pelo relatório
     * de pacientes para evitar N+1 (uma query por paciente). Só o peso é necessário nesse contexto,
     * não o id da avaliação.
     */
    public Map<Long, BigDecimal> buscarPesosMaisRecentesPorPaciente(List<Long> pacienteIds) {
        if (pacienteIds.isEmpty()) {
            return Map.of();
        }
        QAvaliacao a = QAvaliacao.avaliacao;
        List<Avaliacao> todas = contexto.getQueryFactory()
                .selectFrom(a)
                .where(a.organizacao.id.eq(contexto.getContextoDeAutenticacao().organizacaoAtualId())
                        .and(a.paciente.id.in(pacienteIds))
                        .and(a.peso.isNotNull())
                        .and(a.data.loe(LocalDate.now())))
                .orderBy(a.data.desc(), a.id.desc())
                .fetch();

        Map<Long, BigDecimal> pesoPorPaciente = new LinkedHashMap<>();
        for (Avaliacao item : todas) {
            pesoPorPaciente.putIfAbsent(item.getPaciente().getId(), item.getPeso());
        }
        return pesoPorPaciente;
    }

    private List<Avaliacao> buscarAvaliacoes(Long pacienteId, String busca) {
        QAvaliacao avaliacao = QAvaliacao.avaliacao;

        BooleanExpression filtroOrganizacao = avaliacao.organizacao.id.eq(contexto.getContextoDeAutenticacao().organizacaoAtualId());
        BooleanExpression filtroPaciente = pacienteId == null ? null : avaliacao.paciente.id.eq(pacienteId);
        BooleanExpression filtroBusca = busca == null ? null : avaliacao.paciente.nome.containsIgnoreCase(busca);

        return contexto.getQueryFactory()
                .selectFrom(avaliacao)
                .join(avaliacao.paciente).fetchJoin()
                .join(avaliacao.avaliador).fetchJoin()
                .where(filtroOrganizacao, filtroPaciente, filtroBusca)
                .orderBy(avaliacao.data.desc())
                .fetch();
    }

    /**
     * Calcula a variação de peso de cada avaliação em memória, agrupando por paciente — evita N+1
     * de uma query por linha. A variação é sempre relativa ao histórico completo do paciente, não
     * só ao subconjunto retornado por este método (mesmo raciocínio do relatório, ver
     * AvaliacaoRelatorioService).
     */
    private List<AvaliacaoResponseDTO> comVariacao(List<Avaliacao> avaliacoes) {
        Map<Long, BigDecimal> variacaoPorId = calcularVariacoesPorPaciente(avaliacoes);
        return avaliacoes.stream()
                .map(item -> AvaliacaoResponseDTO.from(item, variacaoPorId.get(item.getId())))
                .toList();
    }

    public static Map<Long, BigDecimal> calcularVariacoesPorPaciente(List<Avaliacao> avaliacoes) {
        Map<Long, BigDecimal> variacaoPorId = new HashMap<>();
        Map<Long, List<Avaliacao>> porPaciente = avaliacoes.stream()
                .collect(Collectors.groupingBy(item -> item.getPaciente().getId()));

        for (List<Avaliacao> doPaciente : porPaciente.values()) {
            List<Avaliacao> ordenadas = doPaciente.stream()
                    .sorted(Comparator.comparing(Avaliacao::getData))
                    .toList();
            BigDecimal pesoAnterior = null;
            for (Avaliacao item : ordenadas) {
                if (item.getPeso() != null) {
                    if (pesoAnterior != null) {
                        variacaoPorId.put(item.getId(), item.getPeso().subtract(pesoAnterior));
                    }
                    pesoAnterior = item.getPeso();
                }
            }
        }
        return variacaoPorId;
    }

    private BigDecimal calcularVariacao(Avaliacao avaliacao) {
        if (avaliacao.getPeso() == null) {
            return null;
        }
        QAvaliacao a = QAvaliacao.avaliacao;
        Avaliacao anterior = contexto.getQueryFactory()
                .selectFrom(a)
                .where(a.paciente.id.eq(avaliacao.getPaciente().getId())
                        .and(a.data.lt(avaliacao.getData()))
                        .and(a.peso.isNotNull())
                        .and(a.id.ne(avaliacao.getId())))
                .orderBy(a.data.desc())
                .fetchFirst();

        return anterior == null ? null : avaliacao.getPeso().subtract(anterior.getPeso());
    }

    private String normalizarBusca(String busca) {
        return (busca == null || busca.isBlank()) ? null : busca.trim();
    }

    private void validarDataNaoFutura(LocalDate data, BigDecimal peso) {
        if (peso != null && data != null && data.isAfter(LocalDate.now())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("avaliacao.data.futura"));
        }
    }

    private Paciente buscarPacienteOuFalhar(Long pacienteId) {
        return pacienteRepository
                .findByIdAndOrganizacaoId(pacienteId, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("paciente.naoEncontrado", pacienteId)));
    }

    /**
     * Avaliador válido = Usuario com Vinculo ativo na organização atual — mesma regra de
     * {@code ProntuarioService.buscarAutorOuFalhar}.
     */
    private Usuario buscarAvaliadorOuFalhar(Long avaliadorId) {
        Long organizacaoAtualId = contexto.getContextoDeAutenticacao().organizacaoAtualId();
        return vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(avaliadorId).stream()
                .filter(vinculo -> vinculo.getOrganizacao().getId().equals(organizacaoAtualId))
                .map(Vinculo::getUsuario)
                .findFirst()
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST,
                        resolveMessage("avaliacao.avaliador.invalido", avaliadorId)));
    }

    private Avaliacao buscarAvaliacaoOuFalhar(Long id) {
        return avaliacaoRepository
                .findByIdAndOrganizacaoId(id, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("avaliacao.naoEncontrada", id)));
    }

    private String resolveMessage(String key, Object... args) {
        return contexto.getMensagens().getMessage(key, args);
    }
}
