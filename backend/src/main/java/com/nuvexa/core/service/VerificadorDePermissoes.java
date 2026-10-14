package com.nuvexa.core.service;

import com.nuvexa.core.model.Permissao;
import com.nuvexa.platform.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Responde se o papel do vínculo ativo do usuário na organização atual possui a permissão. Usado
 * por {@code @PreAuthorize("@permissoes.pode('CLINICO')")}. Complementa o escopo de organização
 * das queries e o portão de especialidade — não os substitui.
 */
@Component("permissoes")
@RequiredArgsConstructor
public class VerificadorDePermissoes {

    private final ContextoDeAutenticacao contextoDeAutenticacao;

    public boolean pode(String permissao) {
        Permissao exigida = Permissao.valueOf(permissao);
        try {
            return contextoDeAutenticacao.vinculoAtual().getPapel().possui(exigida);
        } catch (NegocioException ex) {
            // Sem usuário ou sem vínculo ativo: nega (403) em vez de virar 500 dentro do SpEL.
            return false;
        }
    }
}
