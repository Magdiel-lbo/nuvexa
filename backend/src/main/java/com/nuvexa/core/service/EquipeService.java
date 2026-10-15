package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.MembroPapelRequestDTO;
import com.nuvexa.core.dto.response.MembroResponseDTO;
import com.nuvexa.core.model.PapelOrganizacional;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.platform.auditoria.AuditoriaService;
import com.nuvexa.platform.auditoria.EntidadeAuditavel;
import com.nuvexa.platform.auditoria.TipoEventoAuditoria;
import com.nuvexa.platform.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Membros da organização atual. Mesmas regras do convite: ninguém altera nem remove quem tem papel
 * acima do próprio, nem concede papel acima do próprio. A organização nunca fica sem PROPRIETARIO.
 * Remover é desativar o vínculo (o histórico fica); o acesso cai na requisição seguinte, porque o
 * vínculo é lido do banco a cada chamada. O usuário e a conta dele não são tocados.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class EquipeService {

    private final VinculoRepository vinculoRepository;
    private final ContextoDeAutenticacao contextoDeAutenticacao;
    private final AuditoriaService auditoriaService;
    private final MessageSourceAccessor mensagens;

    @Transactional(readOnly = true)
    public List<MembroResponseDTO> listar() {
        return vinculoRepository.findByOrganizacaoIdOrderByAtivoDescUsuarioNomeAsc(contextoDeAutenticacao.organizacaoAtualId()).stream()
                .map(MembroResponseDTO::from)
                .toList();
    }

    public MembroResponseDTO alterarPapel(Long vinculoId, MembroPapelRequestDTO request) {
        Vinculo executor = contextoDeAutenticacao.vinculoAtual();
        Vinculo alvo = buscarAtivoOuFalhar(vinculoId, executor);
        PapelOrganizacional papelAnterior = alvo.getPapel();
        garantirPodeAgirSobre(executor, alvo, request.getPapel());
        if (papelAnterior == PapelOrganizacional.PROPRIETARIO && request.getPapel() != PapelOrganizacional.PROPRIETARIO) {
            garantirOutroProprietario(alvo);
        }

        alvo.setPapel(request.getPapel());
        registrar(alvo, TipoEventoAuditoria.EDICAO, "papel=" + papelAnterior, "papel=" + request.getPapel());
        log.info("Papel do vínculo id={} alterado de {} para {}", vinculoId, papelAnterior, request.getPapel());
        return MembroResponseDTO.from(alvo);
    }

    public void remover(Long vinculoId) {
        Vinculo executor = contextoDeAutenticacao.vinculoAtual();
        Vinculo alvo = buscarAtivoOuFalhar(vinculoId, executor);
        garantirPodeAgirSobre(executor, alvo, alvo.getPapel());
        if (alvo.getPapel() == PapelOrganizacional.PROPRIETARIO) {
            garantirOutroProprietario(alvo);
        }

        alvo.setAtivo(false);
        registrar(alvo, TipoEventoAuditoria.REVOGACAO, "papel=" + alvo.getPapel() + ", ativo=true", "ativo=false");
        log.info("Vínculo id={} desativado", vinculoId);
    }

    private Vinculo buscarAtivoOuFalhar(Long vinculoId, Vinculo executor) {
        return vinculoRepository.findByIdAndOrganizacaoId(vinculoId, executor.getOrganizacao().getId())
                .filter(Vinculo::isAtivo)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, mensagens.getMessage("equipe.membroNaoEncontrado", new Object[]{vinculoId})));
    }

    private void garantirPodeAgirSobre(Vinculo executor, Vinculo alvo, PapelOrganizacional papelNovo) {
        PapelOrganizacional papelDoExecutor = executor.getPapel();
        if (!papelDoExecutor.podeConceder(alvo.getPapel()) || !papelDoExecutor.podeConceder(papelNovo)) {
            throw new NegocioException(HttpStatus.FORBIDDEN, mensagens.getMessage("equipe.semPermissaoSobreMembro"));
        }
    }

    private void garantirOutroProprietario(Vinculo alvo) {
        long proprietarios = vinculoRepository.findByOrganizacaoIdAndAtivoTrueOrderByUsuario_NomeAsc(alvo.getOrganizacao().getId()).stream()
                .filter(vinculo -> vinculo.getPapel() == PapelOrganizacional.PROPRIETARIO)
                .count();
        if (proprietarios <= 1) {
            throw new NegocioException(HttpStatus.CONFLICT, mensagens.getMessage("equipe.ultimoProprietario"));
        }
    }

    private void registrar(Vinculo alvo, TipoEventoAuditoria tipo, String antes, String depois) {
        Usuario executor = contextoDeAutenticacao.usuarioAtual();
        auditoriaService.registrar(EntidadeAuditavel.VINCULO, alvo.getId(), tipo, alvo.getOrganizacao().getId(),
                executor.getId(), executor.getNome(), "usuario=" + alvo.getUsuario().getEmail() + ", " + antes, depois);
    }
}
