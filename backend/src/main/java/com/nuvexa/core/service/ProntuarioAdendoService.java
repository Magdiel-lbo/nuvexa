package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.ProntuarioAdendoCreateRequestDTO;
import com.nuvexa.core.dto.response.ProntuarioAdendoResponseDTO;
import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.ProntuarioAdendo;
import com.nuvexa.core.model.StatusProntuario;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.ProntuarioAdendoRepository;
import com.nuvexa.core.repository.ProntuarioRepository;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.platform.auditoria.AuditoriaService;
import com.nuvexa.platform.auditoria.EntidadeAuditavel;
import com.nuvexa.platform.auditoria.TipoEventoAuditoria;
import com.nuvexa.platform.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Só cria e lista — de propósito não tem update/delete, mesma garantia de imutabilidade de
 * {@link ProntuarioAdendo}.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ProntuarioAdendoService {

    private final ProntuarioAdendoRepository prontuarioAdendoRepository;
    private final ProntuarioRepository prontuarioRepository;
    private final VinculoRepository vinculoRepository;
    private final AuditoriaService auditoriaService;
    private final OrganizacaoScopedContext contexto;

    public ProntuarioAdendoResponseDTO create(Long prontuarioId, ProntuarioAdendoCreateRequestDTO request) {
        if (request.getTexto() == null || request.getTexto().isBlank()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("prontuarioAdendo.texto.obrigatorio"));
        }
        Prontuario prontuario = buscarProntuarioOuFalhar(prontuarioId);
        if (prontuario.getStatus() != StatusProntuario.ASSINADO) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("prontuarioAdendo.prontuario.naoAssinado"));
        }
        Usuario autor = buscarAutorOuFalhar(request.getAutorId());

        ProntuarioAdendo adendo = prontuarioAdendoRepository.save(
                request.toProntuarioAdendo(contexto.getContextoDeAutenticacao().organizacaoAtual(), prontuario, autor));

        Usuario usuarioAtual = contexto.getContextoDeAutenticacao().usuarioAtual();
        auditoriaService.registrar(EntidadeAuditavel.PRONTUARIO, prontuario.getId(), TipoEventoAuditoria.ADENDO,
                contexto.getContextoDeAutenticacao().organizacaoAtualId(), usuarioAtual.getId(), usuarioAtual.getNome(),
                null, "adendo id=" + adendo.getId() + ": " + adendo.getTexto());

        log.info("Adendo criado com id={} para prontuarioId={}", adendo.getId(), prontuarioId);
        return ProntuarioAdendoResponseDTO.from(adendo);
    }

    public List<ProntuarioAdendoResponseDTO> findAll(Long prontuarioId) {
        buscarProntuarioOuFalhar(prontuarioId);
        return prontuarioAdendoRepository
                .findByProntuarioIdAndOrganizacaoIdOrderByCriadoEmAsc(prontuarioId, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .stream()
                .map(ProntuarioAdendoResponseDTO::from)
                .toList();
    }

    private Prontuario buscarProntuarioOuFalhar(Long id) {
        return prontuarioRepository
                .findByIdAndOrganizacaoId(id, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("prontuario.naoEncontrado", id)));
    }

    /**
     * Mesma regra de {@code ProntuarioService.buscarAutorOuFalhar}: autor válido = Usuario com
     * Vinculo ativo na organização atual.
     */
    private Usuario buscarAutorOuFalhar(Long autorId) {
        Long organizacaoAtualId = contexto.getContextoDeAutenticacao().organizacaoAtualId();
        return vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(autorId).stream()
                .filter(vinculo -> vinculo.getOrganizacao().getId().equals(organizacaoAtualId))
                .map(Vinculo::getUsuario)
                .findFirst()
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST,
                        resolveMessage("prontuario.autor.invalido", autorId)));
    }

    private String resolveMessage(String key, Object... args) {
        return contexto.getMensagens().getMessage(key, args);
    }
}
