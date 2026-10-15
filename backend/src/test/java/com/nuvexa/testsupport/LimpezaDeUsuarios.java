package com.nuvexa.testsupport;

import org.springframework.jdbc.core.JdbcTemplate;

/** Os eventos de autenticação/auditoria referenciam o usuário por FK: apagam-se antes dele. */
public final class LimpezaDeUsuarios {

    private LimpezaDeUsuarios() {
    }

    public static void removerEventosDe(JdbcTemplate jdbc, Long usuarioId) {
        jdbc.update("DELETE FROM eventos_autenticacao WHERE usuario_id = ? OR email_informado = (SELECT email FROM usuarios WHERE id = ?)", usuarioId, usuarioId);
        jdbc.update("DELETE FROM eventos_auditoria WHERE usuario_id = ?", usuarioId);
    }
}
