package com.nuvexa.core.vinculo.model;

/**
 * Papel do usuário <b>dentro de uma organização</b>.
 * Não confundir com {@link com.nuvexa.core.identity.model.Perfil}, que é o papel de acesso
 * à plataforma (ADMIN/PROFISSIONAL).
 */
public enum PapelOrganizacional {
    PROPRIETARIO,
    GESTOR,
    MEMBRO
}
