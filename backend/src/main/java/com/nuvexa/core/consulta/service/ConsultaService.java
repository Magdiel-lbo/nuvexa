package com.nuvexa.core.consulta.service;

import com.nuvexa.core.consulta.dto.request.ConsultaCreateRequestDTO;
import com.nuvexa.core.consulta.dto.request.ConsultaUpdateRequestDTO;
import com.nuvexa.core.consulta.dto.response.ConsultaResponseDTO;
import com.nuvexa.core.consulta.model.Consulta;
import com.nuvexa.core.consulta.model.QConsulta;
import com.nuvexa.core.consulta.repository.ConsultaRepository;
import com.nuvexa.core.contexto.ContextoDeAutenticacao;
import com.nuvexa.core.paciente.model.Paciente;
import com.nuvexa.core.paciente.repository.PacienteRepository;
import com.nuvexa.platform.exception.NegocioException;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ConsultaService {

    private static final Locale MESSAGE_LOCALE = Locale.of("pt", "BR");

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ContextoDeAutenticacao contextoDeAutenticacao;
    private final JPAQueryFactory queryFactory;
    private final MessageSource messageSource;
    private final ModelMapper modelMapper;

    public ConsultaResponseDTO create(ConsultaCreateRequestDTO request) {
        Paciente paciente = buscarPacienteOuFalhar(request.getPacienteId());

        Consulta consulta = consultaRepository.save(
                request.toConsulta(contextoDeAutenticacao.organizacaoAtual(), paciente));
        log.info("Consulta criada com id={}", consulta.getId());
        return ConsultaResponseDTO.from(consulta);
    }

    public ConsultaResponseDTO update(Long id, ConsultaUpdateRequestDTO request) {
        Consulta consulta = buscarConsultaOuFalhar(id);
        request.atualizar(consulta, modelMapper);

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

    public void delete(Long id) {
        Consulta consulta = buscarConsultaOuFalhar(id);
        consultaRepository.delete(consulta);
        log.info("Consulta removida com id={}", id);
    }

    private List<Consulta> buscarConsultas(Long pacienteId, String busca) {
        QConsulta consulta = QConsulta.consulta;

        // Escopo aplicado na própria query, mesmo padrão de PacienteService: nenhuma consulta de
        // outra organização chega ao Java.
        BooleanExpression filtroOrganizacao = consulta.organizacao.id.eq(contextoDeAutenticacao.organizacaoAtualId());
        BooleanExpression filtroPaciente = pacienteId == null ? null : consulta.paciente.id.eq(pacienteId);
        BooleanExpression filtroBusca = busca == null ? null : consulta.paciente.nome.containsIgnoreCase(busca);

        return queryFactory
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
        return pacienteRepository
                .findByIdAndOrganizacaoId(pacienteId, contextoDeAutenticacao.organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("paciente.naoEncontrado", pacienteId)));
    }

    private Consulta buscarConsultaOuFalhar(Long id) {
        return consultaRepository
                .findByIdAndOrganizacaoId(id, contextoDeAutenticacao.organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("consulta.naoEncontrada", id)));
    }

    private String resolveMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, MESSAGE_LOCALE);
    }
}
