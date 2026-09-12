package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.ConsultaCreateRequestDTO;
import com.nuvexa.core.dto.request.ConsultaUpdateRequestDTO;
import com.nuvexa.core.dto.response.ConsultaResponseDTO;
import com.nuvexa.core.dto.response.ProfissionalResponseDTO;
import com.nuvexa.core.model.Consulta;
import com.nuvexa.core.model.QConsulta;
import com.nuvexa.core.model.StatusConsulta;
import com.nuvexa.core.repository.ConsultaRepository;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.platform.exception.NegocioException;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final VinculoRepository vinculoRepository;
    private final ModelMapper modelMapper;
    private final OrganizacaoScopedContext contexto;
    private final ValidadorOrganizacional validadorOrganizacional;

    public ConsultaResponseDTO create(ConsultaCreateRequestDTO request) {
        Paciente paciente = buscarPacienteOuFalhar(request.getPacienteId());
        Usuario profissional = buscarProfissionalOuFalhar(request.getProfissionalId());
        Long organizacaoId = contexto.getContextoDeAutenticacao().organizacaoAtualId();
        garantirSemConflitoDeHorario(organizacaoId, profissional.getId(), paciente.getId(),
                request.getDataHora(), request.getDuracaoMinutos(), request.getStatus(), null);

        Consulta consulta = consultaRepository.save(
                request.toConsulta(contexto.getContextoDeAutenticacao().organizacaoAtual(), paciente, profissional));
        log.info("Consulta criada com id={}", consulta.getId());
        return ConsultaResponseDTO.from(consulta);
    }

    public ConsultaResponseDTO update(Long id, ConsultaUpdateRequestDTO request) {
        Consulta consulta = buscarConsultaOuFalhar(id);
        garantirEditavel(consulta);
        Usuario profissional = buscarProfissionalOuFalhar(request.getProfissionalId());
        request.atualizar(consulta, profissional, modelMapper);

        Long organizacaoId = contexto.getContextoDeAutenticacao().organizacaoAtualId();
        garantirSemConflitoDeHorario(organizacaoId, consulta.getProfissional().getId(), consulta.getPaciente().getId(),
                consulta.getDataHora(), consulta.getDuracaoMinutos(), consulta.getStatus(), consulta.getId());

        Consulta saved = consultaRepository.save(consulta);
        log.info("Consulta atualizada com id={}", id);
        return ConsultaResponseDTO.from(saved);
    }

    public ConsultaResponseDTO findById(Long id) {
        return ConsultaResponseDTO.from(buscarConsultaOuFalhar(id));
    }

    public List<ConsultaResponseDTO> findAll(Long pacienteId, String busca) {
        return buscarConsultas(pacienteId, normalizarBusca(busca)).stream()
                .map(ConsultaResponseDTO::from)
                .toList();
    }

    /**
     * Profissionais elegíveis para atender consulta na organização atual — mesma regra de
     * "profissional válido" usada em {@link #buscarProfissionalOuFalhar}, para alimentar o
     * seletor de profissional no frontend.
     */
    public List<ProfissionalResponseDTO> listarProfissionais() {
        return vinculoRepository.findByOrganizacaoIdAndAtivoTrueOrderByUsuario_NomeAsc(
                        contexto.getContextoDeAutenticacao().organizacaoAtualId()).stream()
                .map(Vinculo::getUsuario)
                .map(ProfissionalResponseDTO::from)
                .toList();
    }

    public void delete(Long id) {
        Consulta consulta = buscarConsultaOuFalhar(id);
        consultaRepository.delete(consulta);
        log.info("Consulta removida com id={}", id);
    }

    private List<Consulta> buscarConsultas(Long pacienteId, String busca) {
        QConsulta consulta = QConsulta.consulta;

        // Escopo aplicado na própria query, mesmo padrão de PacienteService: nenhuma consulta de
        // outra organização chega ao Java.
        BooleanExpression filtroOrganizacao = consulta.organizacao.id.eq(contexto.getContextoDeAutenticacao().organizacaoAtualId());
        BooleanExpression filtroPaciente = pacienteId == null ? null : consulta.paciente.id.eq(pacienteId);
        BooleanExpression filtroBusca = busca == null ? null : consulta.paciente.nome.containsIgnoreCase(busca);

        return contexto.getQueryFactory()
                .selectFrom(consulta)
                .join(consulta.paciente).fetchJoin()
                .where(filtroOrganizacao, filtroPaciente, filtroBusca)
                .orderBy(consulta.dataHora.desc())
                .fetch();
    }

    private String normalizarBusca(String busca) {
        return (busca == null || busca.isBlank()) ? null : busca.trim();
    }

    /**
     * Busca sempre restrita à organização atual — mesma regra de Paciente: id de outra
     * organização vira 404, não 403 (um 403 confirmaria que aquele id existe).
     */
    private Paciente buscarPacienteOuFalhar(Long pacienteId) {
        return validadorOrganizacional.pacienteDaOrganizacao(pacienteId, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("paciente.naoEncontrado", pacienteId)));
    }

    /**
     * Profissional válido = Usuario com Vinculo ativo na organização atual. Mesma regra e
     * mesmo motivo de {@code PacienteProfissionalService.buscarProfissionalOuFalhar}: não usa
     * Usuario.perfil como critério, para não confundir papel de plataforma com papel
     * organizacional.
     */
    private Usuario buscarProfissionalOuFalhar(Long profissionalId) {
        return validadorOrganizacional.usuarioAtivoNaOrganizacao(profissionalId, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST,
                        resolveMessage("consulta.profissional.invalido", profissionalId)));
    }

    /**
     * REALIZADA/CANCELADA/FALTOU são status terminais — sem transição de saída (nenhum dos três
     * volta a ser AGENDADA/CONFIRMADA nem muda pra outro terminal). Bloqueia o update inteiro
     * nesse caso, não só o campo status, mesmo padrão de {@code ProntuarioService.garantirEditavel}
     * / {@code AvaliacaoService.garantirEditavel}.
     */
    private void garantirEditavel(Consulta consulta) {
        StatusConsulta status = consulta.getStatus();
        if (status == StatusConsulta.REALIZADA || status == StatusConsulta.CANCELADA || status == StatusConsulta.FALTOU) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("consulta.finalizada.imutavel"));
        }
    }

    /**
     * Sem conflito = nenhuma outra consulta da mesma organização, do mesmo profissional OU do
     * mesmo paciente, com status diferente de CANCELADA, ocupa um intervalo de tempo sobreposto
     * a [dataHora, dataHora + duracaoMinutos). Consulta sendo (re)agendada como CANCELADA não
     * precisa da checagem — não vai ocupar horário nenhum. No update, a própria consulta é
     * excluída da comparação via {@code consultaIdExcluida}.
     */
    private void garantirSemConflitoDeHorario(Long organizacaoId, Long profissionalId, Long pacienteId,
            LocalDateTime dataHora, Integer duracaoMinutos, StatusConsulta status, Long consultaIdExcluida) {
        if (status == StatusConsulta.CANCELADA) {
            return;
        }
        LocalDateTime inicio = dataHora;
        LocalDateTime fim = dataHora.plusMinutes(duracaoMinutos);

        QConsulta consulta = QConsulta.consulta;
        if (existeConflito(consulta.profissional.id, profissionalId, organizacaoId, inicio, fim, consultaIdExcluida)) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("consulta.conflito.profissional"));
        }
        if (existeConflito(consulta.paciente.id, pacienteId, organizacaoId, inicio, fim, consultaIdExcluida)) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("consulta.conflito.paciente"));
        }
    }

    /**
     * Filtra no banco por organização/campo/status (reduz o que chega ao Java), e resolve a
     * sobreposição exata (`existenteFim > inicio`) em memória — evita expressar aritmética de
     * data dinâmica (coluna + coluna) no QueryDSL só para um conjunto que já é pequeno por
     * profissional/paciente.
     */
    private boolean existeConflito(NumberPath<Long> campo, Long valor, Long organizacaoId,
            LocalDateTime inicio, LocalDateTime fim, Long consultaIdExcluida) {
        QConsulta consulta = QConsulta.consulta;
        BooleanExpression filtro = consulta.organizacao.id.eq(organizacaoId)
                .and(campo.eq(valor))
                .and(consulta.status.ne(StatusConsulta.CANCELADA))
                .and(consulta.dataHora.lt(fim));
        if (consultaIdExcluida != null) {
            filtro = filtro.and(consulta.id.ne(consultaIdExcluida));
        }

        List<Consulta> candidatas = contexto.getQueryFactory()
                .selectFrom(consulta)
                .where(filtro)
                .fetch();

        return candidatas.stream()
                .anyMatch(candidata -> candidata.getDataHora().plusMinutes(candidata.getDuracaoMinutos()).isAfter(inicio));
    }

    private Consulta buscarConsultaOuFalhar(Long id) {
        return consultaRepository
                .findByIdAndOrganizacaoId(id, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("consulta.naoEncontrada", id)));
    }

    private String resolveMessage(String key, Object... args) {
        return contexto.getMensagens().getMessage(key, args);
    }
}
