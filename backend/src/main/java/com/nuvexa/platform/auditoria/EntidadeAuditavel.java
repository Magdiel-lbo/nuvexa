package com.nuvexa.platform.auditoria;

/**
 * Identifica o tipo de entidade auditada em {@link EventoAuditoria}, sem o platform/ precisar
 * conhecer as classes de domínio de core/nutricao — cada vertical só referencia sua própria
 * constante. Novas entidades clínicas auditáveis entram aqui como um novo valor.
 */
public enum EntidadeAuditavel {
    PRONTUARIO,
    PRONTUARIO_ADENDO
}
