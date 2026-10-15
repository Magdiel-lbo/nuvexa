package com.nuvexa.platform.security;

/** Porta para o filtro JWT conferir, a cada requisição, se a sessão do access token segue ativa. */
public interface ValidadorDeSessao {

    boolean ativa(Long sessaoId, String emailDoUsuario);
}
