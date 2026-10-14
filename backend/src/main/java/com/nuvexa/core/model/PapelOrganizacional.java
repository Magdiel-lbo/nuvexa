package com.nuvexa.core.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Papel do usuário <b>dentro de uma organização</b>.
 * Não confundir com {@link com.nuvexa.core.model.Perfil}, que é o papel de acesso
 * à plataforma (ADMIN/PROFISSIONAL).
 *
 * <p>Hoje só RECEPCAO difere dos demais: cadastra e agenda, mas não acessa o dado clínico.
 * PROPRIETARIO, GESTOR e MEMBRO têm as mesmas permissões até existir uma regra que as separe.
 */
public enum PapelOrganizacional {
    PROPRIETARIO(EnumSet.allOf(Permissao.class)),
    GESTOR(EnumSet.allOf(Permissao.class)),
    MEMBRO(EnumSet.allOf(Permissao.class)),
    RECEPCAO(EnumSet.of(Permissao.CADASTRO_AGENDA));

    private final Set<Permissao> permissoes;

    PapelOrganizacional(Set<Permissao> permissoes) {
        this.permissoes = permissoes;
    }

    public boolean possui(Permissao permissao) {
        return permissoes.contains(permissao);
    }
}
