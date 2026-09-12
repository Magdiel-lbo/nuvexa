package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.ProntuarioCreateRequestDTO;
import com.nuvexa.core.dto.request.ProntuarioUpdateRequestDTO;
import com.nuvexa.core.dto.response.ProntuarioEnumsResponseDTO;
import com.nuvexa.core.dto.response.ProntuarioResponseDTO;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.QProntuario;
import com.nuvexa.core.model.StatusProntuario;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.repository.ProntuarioAdendoRepository;
import com.nuvexa.core.repository.ProntuarioAnexoRepository;
import com.nuvexa.core.repository.ProntuarioRepository;
import com.nuvexa.platform.auditoria.AuditoriaService;
import com.nuvexa.platform.auditoria.EntidadeAuditavel;
import com.nuvexa.platform.auditoria.EventoAuditoriaResponseDTO;
import com.nuvexa.platform.auditoria.TipoEventoAuditoria;
import com.nuvexa.platform.exception.NegocioException;
import com.querydsl.core.types.dsl.BooleanExpression;
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
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;
    private final ProntuarioAdendoRepository prontuarioAdendoRepository;
    private final ProntuarioAnexoRepository prontuarioAnexoRepository;
    private final ModelMapper modelMapper;
    private final OrganizacaoScopedContext contexto;
    private final AuditoriaService auditoriaService;
    private final ValidadorOrganizacional validadorOrganizacional;

    public ProntuarioResponseDTO create(ProntuarioCreateRequestDTO request) {
        if (request.getStatus() == StatusProntuario.ASSINADO) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("prontuario.assinatura.usarAcaoDedicada"));
        }
        Paciente paciente = buscarPacienteOuFalhar(request.getPacienteId());
        Usuario autor = buscarAutorOuFalhar(request.getAutorId());

        Prontuario prontuario = prontuarioRepository.save(
                request.toProntuario(contexto.getContextoDeAutenticacao().organizacaoAtual(), paciente, autor));

        registrarAuditoria(prontuario.getId(), TipoEventoAuditoria.CRIACAO, null, descrever(prontuario));

        log.info("Prontuário criado com id={}", prontuario.getId());
        return ProntuarioResponseDTO.from(prontuario);
    }

    public ProntuarioResponseDTO update(Long id, ProntuarioUpdateRequestDTO request) {
        Prontuario prontuario = buscarProntuarioOuFalhar(id);
        garantirEditavel(prontuario);
        if (request.getStatus() == StatusProntuario.ASSINADO) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("prontuario.assinatura.usarAcaoDedicada"));
        }
        String antes = descrever(prontuario);
        Usuario autor = buscarAutorOuFalhar(request.getAutorId());
        request.atualizar(prontuario, autor, modelMapper);

        Prontuario saved = prontuarioRepository.save(prontuario);

        registrarAuditoria(saved.getId(), TipoEventoAuditoria.EDICAO, antes, descrever(saved));

        log.info("Prontuário atualizado com id={}", id);
        return ProntuarioResponseDTO.from(saved);
    }

    public ProntuarioResponseDTO assinar(Long id) {
        Prontuario prontuario = buscarProntuarioOuFalhar(id);
        garantirEditavel(prontuario);
        if (prontuario.getConteudo() == null || prontuario.getConteudo().isBlank()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("prontuario.assinatura.conteudoObrigatorio"));
        }
        String antes = descrever(prontuario);

        Usuario usuarioAtual = contexto.getContextoDeAutenticacao().usuarioAtual();
        prontuario.setStatus(StatusProntuario.ASSINADO);
        prontuario.setAssinadoPor(usuarioAtual);
        prontuario.setAssinadoEm(LocalDateTime.now());

        Prontuario saved = prontuarioRepository.save(prontuario);

        registrarAuditoria(saved.getId(), TipoEventoAuditoria.ASSINATURA, antes, descrever(saved));

        log.info("Prontuário assinado com id={} por usuarioId={}", id, usuarioAtual.getId());
        return ProntuarioResponseDTO.from(saved);
    }

    public List<EventoAuditoriaResponseDTO> listarAuditoria(Long id) {
        buscarProntuarioOuFalhar(id);
        return auditoriaService.listar(EntidadeAuditavel.PRONTUARIO, id, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .stream()
                .map(EventoAuditoriaResponseDTO::from)
                .toList();
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
        garantirEditavel(prontuario);
        garantirSemFilhos(prontuario);
        String antes = descrever(prontuario);
        Long prontuarioId = prontuario.getId();

        prontuarioRepository.delete(prontuario);

        registrarAuditoria(prontuarioId, TipoEventoAuditoria.EXCLUSAO, antes, null);

        log.info("Prontuário removido com id={}", id);
    }

    /**
     * Empacota a resolução de usuário/organização atuais que os 4 sites de auditoria deste
     * service repetiam antes de chamar {@link AuditoriaService#registrar}.
     */
    private void registrarAuditoria(Long prontuarioId, TipoEventoAuditoria tipoEvento, String antes, String depois) {
        Usuario usuarioAtual = contexto.getContextoDeAutenticacao().usuarioAtual();
        auditoriaService.registrar(EntidadeAuditavel.PRONTUARIO, prontuarioId, tipoEvento,
                contexto.getContextoDeAutenticacao().organizacaoAtualId(), usuarioAtual.getId(), usuarioAtual.getNome(),
                antes, depois);
    }

    private void garantirEditavel(Prontuario prontuario) {
        if (prontuario.getStatus() == StatusProntuario.ASSINADO) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("prontuario.assinado.imutavel"));
        }
    }

    /**
     * ProntuarioAdendo/ProntuarioAnexo não têm cascade JPA nem ON DELETE no banco de propósito
     * (nenhuma outra entidade do domínio faz cascata automática) — excluir o pai com filhos
     * violaria a FK. Bloqueia aqui com mensagem clara em vez de deixar estourar a constraint.
     */
    private void garantirSemFilhos(Prontuario prontuario) {
        if (prontuarioAdendoRepository.existsByProntuarioId(prontuario.getId())
                || prontuarioAnexoRepository.existsByProntuarioId(prontuario.getId())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("prontuario.comFilhos.naoExcluivel"));
        }
    }

    /**
     * Snapshot simples dos campos relevantes para o evento de auditoria — não é versionamento
     * completo, só o suficiente para inspecionar o que mudou entre antes/depois.
     */
    private String descrever(Prontuario prontuario) {
        return "secao=%s, status=%s, autorId=%d, comAnexo=%s, conteudo=%s".formatted(
                prontuario.getSecao(), prontuario.getStatus(), prontuario.getAutor().getId(),
                prontuario.isComAnexo(), prontuario.getConteudo());
    }

    private List<Prontuario> buscarProntuarios(Long pacienteId, String busca) {
        QProntuario prontuario = QProntuario.prontuario;

        // Escopo aplicado na própria query, mesmo padrão de ConsultaService: nenhum prontuário de
        // outra organização chega ao Java.
        BooleanExpression filtroOrganizacao = prontuario.organizacao.id.eq(contexto.getContextoDeAutenticacao().organizacaoAtualId());
        BooleanExpression filtroPaciente = pacienteId == null ? null : prontuario.paciente.id.eq(pacienteId);
        BooleanExpression filtroBusca = busca == null ? null : prontuario.paciente.nome.containsIgnoreCase(busca);

        return contexto.getQueryFactory()
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
        return validadorOrganizacional.pacienteDaOrganizacao(pacienteId, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("paciente.naoEncontrado", pacienteId)));
    }

    /**
     * Autor válido = Usuario com Vinculo ativo na organização atual — mesma regra de
     * {@code ConsultaService.buscarProfissionalOuFalhar}.
     */
    private Usuario buscarAutorOuFalhar(Long autorId) {
        return validadorOrganizacional.usuarioAtivoNaOrganizacao(autorId, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST,
                        resolveMessage("prontuario.autor.invalido", autorId)));
    }

    private Prontuario buscarProntuarioOuFalhar(Long id) {
        return prontuarioRepository
                .findByIdAndOrganizacaoId(id, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("prontuario.naoEncontrado", id)));
    }

    private String resolveMessage(String key, Object... args) {
        return contexto.getMensagens().getMessage(key, args);
    }
}
