package com.nuvexa.core.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Papel do usuário <b>dentro de uma organização</b>.
 * Não confundir com {@link com.nuvexa.core.model.Perfil}, que é o papel de acesso
 * à plataforma (ADMIN/PROFISSIONAL).
 *
 * <p>RECEPCAO cadastra e agenda, mas não acessa o dado clínico. PROPRIETARIO e GESTOR gerenciam a
 * equipe (convites e membros); só PROPRIETARIO edita os dados da própria organização.
 */
public enum PapelOrganizacional {
    PROPRIETARIO(EnumSet.allOf(Permissao.class)),
    GESTOR(EnumSet.of(Permissao.CADASTRO_AGENDA, Permissao.CLINICO, Permissao.GERENCIAR_EQUIPE)),
    MEMBRO(EnumSet.of(Permissao.CADASTRO_AGENDA, Permissao.CLINICO)),
    RECEPCAO(EnumSet.of(Permissao.CADASTRO_AGENDA));

    private final Set<Permissao> permissoes;

    PapelOrganizacional(Set<Permissao> permissoes) {
        this.permissoes = permissoes;
    }

    public boolean possui(Permissao permissao) {
        return permissoes.contains(permissao);
    }

    /** Ninguém concede papel acima do próprio: GESTOR convida MEMBRO e RECEPCAO; só PROPRIETARIO concede PROPRIETARIO e GESTOR. */
    public boolean podeConceder(PapelOrganizacional alvo) {
        return switch (this) {
            case PROPRIETARIO -> true;
            case GESTOR -> alvo == MEMBRO || alvo == RECEPCAO;
            default -> false;
        };
    }
}
