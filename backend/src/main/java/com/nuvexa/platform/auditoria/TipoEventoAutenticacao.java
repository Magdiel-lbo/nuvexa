package com.nuvexa.platform.auditoria;

public enum TipoEventoAutenticacao {
    LOGIN_SUCESSO,
    LOGIN_FALHA,
    LOGIN_BLOQUEADO,
    CADASTRO,
    CADASTRO_POR_CONVITE,
    RESET_SOLICITADO,
    RESET_BLOQUEADO,
    TOKEN_INVALIDO,
    TOKEN_BLOQUEADO,
    SENHA_REDEFINIDA,
    REFRESH,
    LOGOUT
}
