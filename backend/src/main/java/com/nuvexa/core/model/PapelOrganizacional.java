package com.nuvexa.core.model;

/**
 * Papel do usuário <b>dentro de uma organização</b>.
 * Não confundir com {@link com.nuvexa.core.model.Perfil}, que é o papel de acesso
 * à plataforma (ADMIN/PROFISSIONAL).
 */
public enum PapelOrganizacional {
    PROPRIETARIO,
    GESTOR,
    MEMBRO
}
