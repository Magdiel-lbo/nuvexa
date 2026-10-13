package com.nuvexa.core.service;

import com.nuvexa.core.model.Especialidade;
import com.nuvexa.platform.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Portão de vertical: responde se a organização atual tem a especialidade habilitada. Usado por
 * {@code @PreAuthorize("@especialidades.permite('NUTRICAO')")} nos controllers de cada vertical.
 *
 * <p>Complementa — não substitui — o escopo de organização das queries: aquele impede ler dado de
 * outra organização, este impede uma organização de usar uma vertical que não contratou.
 */
@Component("especialidades")
@RequiredArgsConstructor
public class VerificadorDeEspecialidade {

    private final ContextoDeAutenticacao contextoDeAutenticacao;

    /**
     * O {@code @Transactional} mantém a sessão aberta para ler {@code organizacao.especialidade}
     * (proxy lazy vindo do vínculo): o {@code @PreAuthorize} roda antes de qualquer transação do
     * service e {@code open-in-view} está desligado — sem isso, {@code LazyInitializationException}.
     */
    @Transactional(readOnly = true)
    public boolean permite(String especialidade) {
        Especialidade exigida = Especialidade.valueOf(especialidade);
        try {
            return contextoDeAutenticacao.organizacaoAtual().getEspecialidade() == exigida;
        } catch (NegocioException ex) {
            // Sem vínculo ativo: nega o acesso (403) em vez de deixar a exceção virar 500 dentro da avaliação do SpEL.
            return false;
        }
    }
}
