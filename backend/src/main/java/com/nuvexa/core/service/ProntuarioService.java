package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.ProntuarioCreateRequestDTO;
import com.nuvexa.core.dto.request.ProntuarioUpdateRequestDTO;
import com.nuvexa.core.dto.response.ProntuarioEnumsResponseDTO;
import com.nuvexa.core.dto.response.ProntuarioResponseDTO;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.QProntuario;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.core.repository.ProntuarioRepository;
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
public class ProntuarioService {

    private static final Locale MESSAGE_LOCALE = Locale.of("pt", "BR");

    private final ProntuarioRepository prontuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final VinculoRepository vinculoRepository;
    private final ContextoDeAutenticacao contextoDeAutenticacao;
    private final JPAQueryFactory queryFactory;
    private final MessageSource messageSource;
    private final ModelMapper modelMapper;

    public ProntuarioResponseDTO create(ProntuarioCreateRequestDTO request) {
        Paciente paciente = buscarPacienteOuFalhar(request.getPacienteId());
        Usuario autor = buscarAutorOuFalhar(request.getAutorId());

        Prontuario prontuario = prontuarioRepository.save(
                request.toProntuario(contextoDeAutenticacao.organizacaoAtual(), paciente, autor));
        log.info("Prontuário criado com id={}", prontuario.getId());
        return ProntuarioResponseDTO.from(prontuario);
    }

    public ProntuarioResponseDTO update(Long id, ProntuarioUpdateRequestDTO request) {
        Prontuario prontuario = buscarProntuarioOuFalhar(id);
        Usuario autor = buscarAutorOuFalhar(request.getAutorId());
        request.atualizar(prontuario, autor, modelMapper);

        Prontuario saved = prontuarioRepository.save(prontuario);
        log.info("Prontuário atualizado com id={}", id);
        return ProntuarioResponseDTO.from(saved);
    }

    public ProntuarioResponseDTO findById(Long id) {
        return ProntuarioResponseDTO.from(buscarProntuarioOuFalhar(id));
    }

    public List<ProntuarioResponseDTO> findAll(Long pacienteId, String busca) {
        return buscarProntuarios(pacienteId, normalizarBusca(busca)).stream()
                .map(ProntuarioResponseDTO::from)
                .toList();
    }

    public ProntuarioEnumsResponseDTO getEnums() {
        return ProntuarioEnumsResponseDTO.of();
    }

    public void delete(Long id) {
        Prontuario prontuario = buscarProntuarioOuFalhar(id);
        prontuarioRepository.delete(prontuario);
        log.info("Prontuário removido com id={}", id);
    }

    private List<Prontuario> buscarProntuarios(Long pacienteId, String busca) {
        QProntuario prontuario = QProntuario.prontuario;

        // Escopo aplicado na própria query, mesmo padrão de ConsultaService: nenhum prontuário de
        // outra organização chega ao Java.
        BooleanExpression filtroOrganizacao = prontuario.organizacao.id.eq(contextoDeAutenticacao.organizacaoAtualId());
        BooleanExpression filtroPaciente = pacienteId == null ? null : prontuario.paciente.id.eq(pacienteId);
        BooleanExpression filtroBusca = busca == null ? null : prontuario.paciente.nome.containsIgnoreCase(busca);

        return queryFactory
                .selectFrom(prontuario)
                .join(prontuario.paciente).fetchJoin()
                .join(prontuario.autor).fetchJoin()
                .where(filtroOrganizacao, filtroPaciente, filtroBusca)
                .orderBy(prontuario.atualizadoEm.desc())
                .fetch();
    }

    private String normalizarBusca(String busca) {
        return (busca == null || busca.isBlank()) ? null : busca.trim();
    }

    private Paciente buscarPacienteOuFalhar(Long pacienteId) {
        return pacienteRepository
                .findByIdAndOrganizacaoId(pacienteId, contextoDeAutenticacao.organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("paciente.naoEncontrado", pacienteId)));
    }

    /**
     * Autor válido = Usuario com Vinculo ativo na organização atual — mesma regra de
     * {@code ConsultaService.buscarProfissionalOuFalhar}.
     */
    private Usuario buscarAutorOuFalhar(Long autorId) {
        Long organizacaoAtualId = contextoDeAutenticacao.organizacaoAtualId();
        return vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(autorId).stream()
                .filter(vinculo -> vinculo.getOrganizacao().getId().equals(organizacaoAtualId))
                .map(Vinculo::getUsuario)
                .findFirst()
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST,
                        resolveMessage("prontuario.autor.invalido", autorId)));
    }

    private Prontuario buscarProntuarioOuFalhar(Long id) {
        return prontuarioRepository
                .findByIdAndOrganizacaoId(id, contextoDeAutenticacao.organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("prontuario.naoEncontrado", id)));
    }

    private String resolveMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, MESSAGE_LOCALE);
    }
}
