package com.nuvexa.platform.security;

import com.nuvexa.platform.auditoria.AutenticacaoAuditoriaService;
import com.nuvexa.platform.auditoria.EventoAutenticacaoRepository;
import com.nuvexa.platform.auditoria.TipoEventoAutenticacao;
import com.nuvexa.platform.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Limite de tentativas dos endpoints públicos, por janela deslizante, contando os próprios
 * {@code eventos_autenticacao} (sem cache nem estado em memória: vale com várias instâncias).
 * Melhor esforço: rajadas concorrentes podem passar do limite por alguns pedidos.
 *
 * <p>O IP é o da conexão. Atrás de proxy reverso, configure {@code server.forward-headers-strategy}
 * para que o Tomcat o resolva; senão todos os clientes parecem ter o IP do proxy.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class LimiteDeTentativas {

    private final EventoAutenticacaoRepository eventos;
    private final AutenticacaoAuditoriaService auditoria;
    private final MessageSourceAccessor mensagens;

    @Value("${app.security.rate-limit.janela-minutos:15}")
    private long janelaMinutos;

    @Value("${app.security.rate-limit.login-falhas-por-email:5}")
    private long loginFalhasPorEmail;

    @Value("${app.security.rate-limit.login-falhas-por-ip:20}")
    private long loginFalhasPorIp;

    @Value("${app.security.rate-limit.reset-por-email:3}")
    private long resetPorEmail;

    @Value("${app.security.rate-limit.reset-por-ip:10}")
    private long resetPorIp;

    @Value("${app.security.rate-limit.token-invalido-por-ip:10}")
    private long tokenInvalidoPorIp;

    /** Lança 429 (e registra LOGIN_BLOQUEADO) se o e-mail ou o IP excedeu as falhas de login na janela. */
    public void garantirLoginPermitido(String email) {
        LocalDateTime desde = desde();
        String ip = AutenticacaoAuditoriaService.ipDaRequisicao();
        boolean excedeuEmail = eventos.countByTipoEventoAndEmailInformadoAndCriadoEmAfter(TipoEventoAutenticacao.LOGIN_FALHA, email, desde) >= loginFalhasPorEmail;
        boolean excedeuIp = ip != null
                && eventos.countByTipoEventoAndIpAndCriadoEmAfter(TipoEventoAutenticacao.LOGIN_FALHA, ip, desde) >= loginFalhasPorIp;
        if (excedeuEmail || excedeuIp) {
            auditoria.registrar(TipoEventoAutenticacao.LOGIN_BLOQUEADO, null, email);
            throw new NegocioException(HttpStatus.TOO_MANY_REQUESTS, mensagens.getMessage("autenticacao.tentativasExcessivas"));
        }
    }

    /**
     * Pedidos de redefinição de senha: ao exceder, devolve {@code false} em vez de erro, para que a
     * resposta continue idêntica à de e-mail desconhecido (sem revelar o bloqueio nem a conta).
     */
    public boolean resetPermitido(String email) {
        LocalDateTime desde = desde();
        String ip = AutenticacaoAuditoriaService.ipDaRequisicao();
        boolean excedeu = eventos.countByTipoEventoAndEmailInformadoAndCriadoEmAfter(TipoEventoAutenticacao.RESET_SOLICITADO, email, desde) >= resetPorEmail
                || (ip != null && eventos.countByTipoEventoAndIpAndCriadoEmAfter(TipoEventoAutenticacao.RESET_SOLICITADO, ip, desde) >= resetPorIp);
        if (excedeu) {
            auditoria.registrar(TipoEventoAutenticacao.RESET_BLOQUEADO, null, email);
            log.warn("Pedido de redefinição de senha bloqueado por excesso de tentativas");
        }
        return !excedeu;
    }

    /** Antes de validar um token de uso único (redefinição de senha, aceite de convite). */
    public void garantirTokenPermitido() {
        String ip = AutenticacaoAuditoriaService.ipDaRequisicao();
        if (ip != null && eventos.countByTipoEventoAndIpAndCriadoEmAfter(TipoEventoAutenticacao.TOKEN_INVALIDO, ip, desde()) >= tokenInvalidoPorIp) {
            auditoria.registrar(TipoEventoAutenticacao.TOKEN_BLOQUEADO, null, null);
            throw new NegocioException(HttpStatus.TOO_MANY_REQUESTS, mensagens.getMessage("autenticacao.tentativasExcessivas"));
        }
    }

    public void registrarTokenInvalido() {
        auditoria.registrar(TipoEventoAutenticacao.TOKEN_INVALIDO, null, null);
    }

    private LocalDateTime desde() {
        return LocalDateTime.now().minusMinutes(janelaMinutos);
    }
}
