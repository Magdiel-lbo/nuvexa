package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.core.dto.request.PacienteUpdateRequestDTO;
import com.nuvexa.core.dto.response.PacienteEnumsResponseDTO;
import com.nuvexa.core.dto.response.PacienteResponseDTO;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.QPaciente;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.platform.exception.NegocioException;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Paciente genérico (core) — só nome/dataNascimento/sexo. Dado clínico de nutrição
 * (altura/peso/objetivo/IMC...) é responsabilidade de {@code PerfilNutricionalService}, na
 * vertical nutricao, que referencia este Paciente por FK.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final ModelMapper modelMapper;
    private final OrganizacaoScopedContext contexto;
    private final ValidadorOrganizacional validadorOrganizacional;

    public PacienteResponseDTO create(PacienteCreateRequestDTO request) {
        validar(request.getDataNascimento());
        Paciente paciente = pacienteRepository.save(request.toPaciente(contexto.getContextoDeAutenticacao().organizacaoAtual()));
        log.info("Paciente criado com id={}", paciente.getId());
        return PacienteResponseDTO.from(paciente);
    }

    public PacienteResponseDTO update(Long id, PacienteUpdateRequestDTO request) {
        validar(request.getDataNascimento());
        Paciente paciente = buscarPacienteOuFalhar(id);
        request.atualizar(paciente, modelMapper);

        Paciente saved = pacienteRepository.save(paciente);
        log.info("Paciente atualizado com id={}", id);
        return PacienteResponseDTO.from(saved);
    }

    public PacienteResponseDTO findById(Long id) {
        return PacienteResponseDTO.from(buscarPacienteOuFalhar(id));
    }

    public List<PacienteResponseDTO> findAll(String busca) {
        return buscarPacientes(normalizarBusca(busca)).stream()
                .map(PacienteResponseDTO::from)
                .toList();
    }

    public PacienteEnumsResponseDTO getEnums() {
        return PacienteEnumsResponseDTO.of();
    }

    private List<Paciente> buscarPacientes(String busca) {
        QPaciente paciente = QPaciente.paciente;

        BooleanExpression filtroOrganizacao = paciente.organizacao.id.eq(contexto.getContextoDeAutenticacao().organizacaoAtualId());
        BooleanExpression filtroBusca = busca == null ? null : paciente.nome.containsIgnoreCase(busca);

        return contexto.getQueryFactory()
                .selectFrom(paciente)
                .where(filtroOrganizacao, filtroBusca)
                .orderBy(paciente.nome.asc())
                .fetch();
    }

    private String normalizarBusca(String busca) {
        return (busca == null || busca.isBlank()) ? null : busca.trim();
    }

    private void validar(LocalDate dataNascimento) {
        if (dataNascimento != null && dataNascimento.isAfter(LocalDate.now())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("paciente.dataNascimento.futura"));
        }
    }

    /**
     * Busca sempre restrita à organização atual. Paciente de outra organização resulta em 404
     * (e não 403) de propósito: um 403 confirmaria ao chamador que aquele id existe.
     */
    private Paciente buscarPacienteOuFalhar(Long id) {
        return validadorOrganizacional.pacienteDaOrganizacao(id, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("paciente.naoEncontrado", id)));
    }

    private String resolveMessage(String key, Object... args) {
        return contexto.getMensagens().getMessage(key, args);
    }
}
