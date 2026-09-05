package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.ConsultaCreateRequestDTO;
import com.nuvexa.core.dto.request.ConsultaUpdateRequestDTO;
import com.nuvexa.core.dto.response.ConsultaResponseDTO;
import com.nuvexa.core.dto.response.ProfissionalResponseDTO;
import com.nuvexa.core.model.Consulta;
import com.nuvexa.core.model.QConsulta;
import com.nuvexa.core.repository.ConsultaRepository;
import com.nuvexa.core.service.ContextoDeAutenticacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.core.repository.VinculoRepository;
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
    private final VinculoRepository vinculoRepository;
    private final ContextoDeAutenticacao contextoDeAutenticacao;
    private final JPAQueryFactory queryFactory;
    private final MessageSource messageSource;
    private final ModelMapper modelMapper;

    public ConsultaResponseDTO create(ConsultaCreateRequestDTO request) {
        Paciente paciente = buscarPacienteOuFalhar(request.getPacienteId());
        Usuario profissional = buscarProfissionalOuFalhar(request.getProfissionalId());

        Consulta consulta = consultaRepository.save(
                request.toConsulta(contextoDeAutenticacao.organizacaoAtual(), paciente, profissional));
        log.info("Consulta criada com id={}", consulta.getId());
        return ConsultaResponseDTO.from(consulta);
    }

    public ConsultaResponseDTO update(Long id, ConsultaUpdateRequestDTO request) {
        Consulta consulta = buscarConsultaOuFalhar(id);
        Usuario profissional = buscarProfissionalOuFalhar(request.getProfissionalId());
        request.atualizar(consulta, profissional, modelMapper);

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
        return vinculoRepository.findByOrganizacaoIdAndAtivoTrueOrderByUsuario_NomeAsc(contextoDeAutenticacao.organizacaoAtualId()).stream()
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

    /**
     * Profissional válido = Usuario com Vinculo ativo na organização atual. Mesma regra e
     * mesmo motivo de {@code PacienteProfissionalService.buscarProfissionalOuFalhar}: não usa
     * Usuario.perfil como critério, para não confundir papel de plataforma com papel
     * organizacional.
     */
    private Usuario buscarProfissionalOuFalhar(Long profissionalId) {
        Long organizacaoAtualId = contextoDeAutenticacao.organizacaoAtualId();
        return vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(profissionalId).stream()
                .filter(vinculo -> vinculo.getOrganizacao().getId().equals(organizacaoAtualId))
                .map(Vinculo::getUsuario)
                .findFirst()
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST,
                        resolveMessage("consulta.profissional.invalido", profissionalId)));
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
