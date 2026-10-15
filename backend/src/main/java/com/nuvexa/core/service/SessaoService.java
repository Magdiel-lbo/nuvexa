package com.nuvexa.core.service;

import com.nuvexa.core.model.Sessao;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.repository.SessaoRepository;
import com.nuvexa.platform.auditoria.AutenticacaoAuditoriaService;
import com.nuvexa.platform.auditoria.TipoEventoAutenticacao;
import com.nuvexa.platform.exception.NegocioException;
import com.nuvexa.platform.security.TokenOpaco;
import com.nuvexa.platform.security.ValidadorDeSessao;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sessões com refresh token rotativo. Cada renovação invalida o refresh anterior; reutilizar um
 * refresh já rotacionado (sinal de token roubado) revoga a família inteira, exceto numa janela
 * curta que tolera duas abas renovando ao mesmo tempo.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class SessaoService implements ValidadorDeSessao {

    static final long TOLERANCIA_DE_CORRIDA_SEGUNDOS = 10;

    private final SessaoRepository sessaoRepository;
    private final AutenticacaoAuditoriaService autenticacaoAuditoria;
    private final MessageSourceAccessor mensagens;

    @Value("${app.jwt.refresh-expiration-days:30}")
    private long refreshDias;

    /** Sessão emitida e o refresh token em claro (só existe aqui; no banco fica o hash). */
    public record Emissao(Sessao sessao, String refreshToken) {
    }

    @Transactional
    public Emissao iniciar(Usuario usuario) {
        return criar(usuario, UUID.randomUUID().toString());
    }

    /**
     * Transação própria e sem rollback em {@link NegocioException}: a revogação da família ao
     * detectar reuso precisa ser gravada mesmo quando a chamada termina em 401.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = NegocioException.class)
    public Emissao rotacionar(String refreshToken) {
        Sessao sessao = sessaoRepository.findByHashRefreshToken(TokenOpaco.hash(refreshToken)).orElseThrow(this::invalido);
        Usuario usuario = sessao.getUsuario();
        Long usuarioId = usuario.getId();
        String email = usuario.getEmail();
        LocalDateTime agora = LocalDateTime.now();

        if (sessao.getRevogadaEm() != null || sessao.getExpiraEm().isBefore(agora)) {
            throw invalido();
        }
        if (sessao.getRotacionadaEm() != null) {
            if (sessao.getRotacionadaEm().isBefore(agora.minusSeconds(TOLERANCIA_DE_CORRIDA_SEGUNDOS))) {
                sessaoRepository.revogarFamilia(sessao.getFamiliaId(), agora);
                autenticacaoAuditoria.registrar(TipoEventoAutenticacao.REFRESH_REUTILIZADO, usuarioId, email);
                log.warn("Refresh token reutilizado; família de sessões do usuário id={} revogada", usuarioId);
            }
            throw invalido();
        }
        if (!usuario.isEnabled()) {
            sessaoRepository.revogarFamilia(sessao.getFamiliaId(), agora);
            throw invalido();
        }

        sessao.setRotacionadaEm(agora);
        return criar(usuario, sessao.getFamiliaId());
    }

    /** Logout: revoga o login inteiro (todas as renovações da mesma família). */
    @Transactional
    public void revogarFamiliaDaSessao(Long sessaoId) {
        sessaoRepository.findById(sessaoId)
                .ifPresent(sessao -> sessaoRepository.revogarFamilia(sessao.getFamiliaId(), LocalDateTime.now()));
    }

    /** Logout de todos os dispositivos e redefinição de senha. */
    @Transactional
    public void revogarTodas(Long usuarioId) {
        sessaoRepository.revogarTodasDoUsuario(usuarioId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean ativa(Long sessaoId, String emailDoUsuario) {
        return sessaoRepository.existeAtiva(sessaoId, emailDoUsuario, LocalDateTime.now());
    }

    private Emissao criar(Usuario usuario, String familiaId) {
        String refreshToken = TokenOpaco.gerar();
        LocalDateTime agora = LocalDateTime.now();
        Sessao sessao = sessaoRepository.save(Sessao.builder()
                .usuario(usuario)
                .familiaId(familiaId)
                .hashRefreshToken(TokenOpaco.hash(refreshToken))
                .expiraEm(agora.plusDays(refreshDias))
                .criadoEm(agora)
                .ip(AutenticacaoAuditoriaService.ipDaRequisicao())
                .userAgent(userAgent())
                .build());
        return new Emissao(sessao, refreshToken);
    }

    private String userAgent() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servlet) {
            String valor = servlet.getRequest().getHeader("User-Agent");
            return valor == null || valor.length() <= 255 ? valor : valor.substring(0, 255);
        }
        return null;
    }

    private NegocioException invalido() {
        return new NegocioException(HttpStatus.UNAUTHORIZED, mensagens.getMessage("autenticacao.refresh.invalido"));
    }
}
