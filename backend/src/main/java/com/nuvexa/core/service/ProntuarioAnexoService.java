package com.nuvexa.core.service;

import com.nuvexa.core.dto.response.ProntuarioAnexoDownloadDTO;
import com.nuvexa.core.dto.response.ProntuarioAnexoResponseDTO;
import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.ProntuarioAnexo;
import com.nuvexa.core.model.StatusProntuario;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.repository.ProntuarioAnexoRepository;
import com.nuvexa.core.repository.ProntuarioRepository;
import com.nuvexa.platform.auditoria.AuditoriaService;
import com.nuvexa.platform.auditoria.EntidadeAuditavel;
import com.nuvexa.platform.auditoria.TipoEventoAuditoria;
import com.nuvexa.platform.exception.NegocioException;
import com.nuvexa.platform.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Só cria, lista, baixa e exclui — sem update, mesma garantia de imutabilidade de
 * {@link ProntuarioAnexo}. Exclusão em prontuário {@code ASSINADO} é bloqueada; upload não.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ProntuarioAnexoService {

    private static final long TAMANHO_MAXIMO_BYTES = 10L * 1024 * 1024;

    private static final Set<String> TIPOS_MIME_PERMITIDOS = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("pdf", "jpg", "jpeg", "png", "doc", "docx", "xls", "xlsx");

    private final ProntuarioAnexoRepository prontuarioAnexoRepository;
    private final ProntuarioRepository prontuarioRepository;
    private final StorageService storageService;
    private final AuditoriaService auditoriaService;
    private final OrganizacaoScopedContext contexto;

    public ProntuarioAnexoResponseDTO upload(Long prontuarioId, MultipartFile arquivo) {
        Prontuario prontuario = buscarProntuarioOuFalhar(prontuarioId);
        validarArquivo(arquivo);

        String nomeOriginal = sanitizarNomeOriginal(arquivo.getOriginalFilename());
        Long organizacaoId = contexto.getContextoDeAutenticacao().organizacaoAtualId();
        String chaveStorage = gerarChaveStorage(organizacaoId, prontuarioId, nomeOriginal);

        byte[] conteudo;
        try {
            conteudo = arquivo.getBytes();
        } catch (IOException e) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("prontuarioAnexo.arquivo.invalido"));
        }

        storageService.upload(chaveStorage, conteudo, arquivo.getContentType());

        Usuario usuarioAtual = contexto.getContextoDeAutenticacao().usuarioAtual();
        ProntuarioAnexo anexo;
        try {
            anexo = prontuarioAnexoRepository.save(ProntuarioAnexo.builder()
                    .organizacao(contexto.getContextoDeAutenticacao().organizacaoAtual())
                    .prontuario(prontuario)
                    .nomeOriginal(nomeOriginal)
                    .chaveStorage(chaveStorage)
                    .tipoMime(arquivo.getContentType())
                    .tamanho(arquivo.getSize())
                    .criadoPor(usuarioAtual)
                    .build());
        } catch (RuntimeException e) {
            // Compensação: já subiu pro storage, mas o registro no banco falhou — não deixar
            // arquivo órfão sem registro nenhum apontando pra ele.
            storageService.delete(chaveStorage);
            throw e;
        }

        auditoriaService.registrar(EntidadeAuditavel.PRONTUARIO, prontuario.getId(), TipoEventoAuditoria.UPLOAD_ANEXO,
                organizacaoId, usuarioAtual.getId(), usuarioAtual.getNome(),
                null, "anexo id=" + anexo.getId() + ": " + nomeOriginal);

        log.info("Anexo criado com id={} para prontuarioId={}", anexo.getId(), prontuarioId);
        return ProntuarioAnexoResponseDTO.from(anexo);
    }

    public List<ProntuarioAnexoResponseDTO> findAll(Long prontuarioId) {
        buscarProntuarioOuFalhar(prontuarioId);
        return prontuarioAnexoRepository
                .findByProntuarioIdAndOrganizacaoIdOrderByCriadoEmAsc(prontuarioId, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .stream()
                .map(ProntuarioAnexoResponseDTO::from)
                .toList();
    }

    public ProntuarioAnexoDownloadDTO download(Long prontuarioId, Long anexoId) {
        buscarProntuarioOuFalhar(prontuarioId);
        ProntuarioAnexo anexo = buscarAnexoOuFalhar(prontuarioId, anexoId);
        byte[] conteudo = storageService.download(anexo.getChaveStorage());
        return new ProntuarioAnexoDownloadDTO(anexo.getNomeOriginal(), anexo.getTipoMime(), conteudo);
    }

    public void delete(Long prontuarioId, Long anexoId) {
        Prontuario prontuario = buscarProntuarioOuFalhar(prontuarioId);
        if (prontuario.getStatus() == StatusProntuario.ASSINADO) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("prontuarioAnexo.assinado.imutavel"));
        }
        ProntuarioAnexo anexo = buscarAnexoOuFalhar(prontuarioId, anexoId);

        prontuarioAnexoRepository.delete(anexo);
        storageService.delete(anexo.getChaveStorage());

        Usuario usuarioAtual = contexto.getContextoDeAutenticacao().usuarioAtual();
        auditoriaService.registrar(EntidadeAuditavel.PRONTUARIO, prontuario.getId(), TipoEventoAuditoria.EXCLUSAO_ANEXO,
                contexto.getContextoDeAutenticacao().organizacaoAtualId(), usuarioAtual.getId(), usuarioAtual.getNome(),
                "anexo id=" + anexo.getId() + ": " + anexo.getNomeOriginal(), null);

        log.info("Anexo removido com id={} do prontuarioId={}", anexoId, prontuarioId);
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("prontuarioAnexo.arquivo.vazio"));
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("prontuarioAnexo.arquivo.tamanhoExcedido"));
        }
        String tipoMime = arquivo.getContentType();
        if (tipoMime == null || !TIPOS_MIME_PERMITIDOS.contains(tipoMime)) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("prontuarioAnexo.arquivo.tipoNaoPermitido"));
        }
        String extensao = extrairExtensao(arquivo.getOriginalFilename());
        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("prontuarioAnexo.arquivo.extensaoNaoPermitida"));
        }
    }

    /**
     * Remove separador de caminho e quebra de linha/aspas (evita path traversal e injeção no
     * header {@code Content-Disposition} do download) — nome original é só metadado de exibição,
     * nunca vira caminho físico.
     */
    private String sanitizarNomeOriginal(String nomeOriginal) {
        String nome = nomeOriginal == null ? "arquivo" : nomeOriginal;
        String limpo = nome.replaceAll("[\\r\\n\"]", "").replaceAll("[/\\\\]", "_").trim();
        if (limpo.isEmpty()) {
            limpo = "arquivo";
        }
        return limpo.length() > 255 ? limpo.substring(0, 255) : limpo;
    }

    private String gerarChaveStorage(Long organizacaoId, Long prontuarioId, String nomeOriginal) {
        String extensao = extrairExtensao(nomeOriginal);
        String base = organizacaoId + "/prontuarios/" + prontuarioId + "/" + UUID.randomUUID();
        return extensao.isEmpty() ? base : base + "." + extensao;
    }

    private String extrairExtensao(String nomeArquivo) {
        if (nomeArquivo == null) {
            return "";
        }
        int idx = nomeArquivo.lastIndexOf('.');
        return idx >= 0 && idx < nomeArquivo.length() - 1 ? nomeArquivo.substring(idx + 1).toLowerCase() : "";
    }

    private Prontuario buscarProntuarioOuFalhar(Long id) {
        return prontuarioRepository
                .findByIdAndOrganizacaoId(id, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("prontuario.naoEncontrado", id)));
    }

    private ProntuarioAnexo buscarAnexoOuFalhar(Long prontuarioId, Long anexoId) {
        Long organizacaoId = contexto.getContextoDeAutenticacao().organizacaoAtualId();
        return prontuarioAnexoRepository.findByIdAndProntuarioIdAndOrganizacaoId(anexoId, prontuarioId, organizacaoId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("prontuarioAnexo.naoEncontrado", anexoId)));
    }

    private String resolveMessage(String key, Object... args) {
        return contexto.getMensagens().getMessage(key, args);
    }
}
