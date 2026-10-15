package com.nuvexa.platform.auditoria;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Registra eventos de autenticação. {@link #registrar} roda em transação PRÓPRIA (REQUIRES_NEW): a
 * falha de login lança exceção e dá rollback na transação do chamador — sem isso o registro se perderia.
 * O IP é o da conexão ({@code getRemoteAddr}); atrás de proxy reverso, configure o Tomcat para
 * resolvê-lo ({@code server.forward-headers-strategy}) em vez de ler cabeçalhos aqui.
 */
@Service
@RequiredArgsConstructor
public class AutenticacaoAuditoriaService {

    private final EventoAutenticacaoRepository eventoAutenticacaoRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(TipoEventoAutenticacao tipo, Long usuarioId, String emailInformado) {
        eventoAutenticacaoRepository.save(EventoAutenticacao.builder()
                .tipoEvento(tipo)
                .usuarioId(usuarioId)
                .emailInformado(truncar(emailInformado, 150))
                .ip(truncar(ipDaRequisicao(), 64))
                .build());
    }

    /**
     * Para eventos de uma conta recém-criada na MESMA transação do chamador: em transação nova o
     * usuário ainda não existiria para a FK. Se o chamador der rollback, o evento some junto.
     */
    @Transactional
    public void registrarNaTransacaoAtual(TipoEventoAutenticacao tipo, Long usuarioId, String emailInformado) {
        eventoAutenticacaoRepository.save(EventoAutenticacao.builder()
                .tipoEvento(tipo)
                .usuarioId(usuarioId)
                .emailInformado(truncar(emailInformado, 150))
                .ip(truncar(ipDaRequisicao(), 64))
                .build());
    }

    public static String ipDaRequisicao() {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        if (atributos instanceof ServletRequestAttributes servlet) {
            HttpServletRequest request = servlet.getRequest();
            return request.getRemoteAddr();
        }
        return null;
    }

    private static String truncar(String valor, int maximo) {
        return valor == null || valor.length() <= maximo ? valor : valor.substring(0, maximo);
    }
}
