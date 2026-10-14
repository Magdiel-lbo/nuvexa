package com.nuvexa.core.model;

/**
 * Capacidade nomeada checada em {@code @PreAuthorize("@permissoes.pode('...')")}. Cada
 * {@link PapelOrganizacional} declara quais possui; o código dos controllers nunca compara papel.
 */
public enum Permissao {
    /** Cadastro de pacientes e agenda de consultas. */
    CADASTRO_AGENDA,
    /** Prontuário, adendos, anexos e dado clínico das verticais (perfil, avaliação, plano). */
    CLINICO
}
