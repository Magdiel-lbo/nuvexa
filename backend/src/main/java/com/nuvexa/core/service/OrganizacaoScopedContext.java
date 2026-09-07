package com.nuvexa.core.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

/**
 * Agrupa os três colaboradores que se repetiam em praticamente todo service de negócio
 * (Consulta, Prontuário, Paciente, PlanoAlimentar, Avaliação e seus *RelatorioService) — um
 * único campo injetado em vez de três, sem precisar de herança (que forçaria construtor
 * explícito em cada subclasse, já que Lombok não encadeia para campos {@code final} herdados).
 */
@Component
@Getter
@RequiredArgsConstructor
public class OrganizacaoScopedContext {

    private final ContextoDeAutenticacao contextoDeAutenticacao;
    private final JPAQueryFactory queryFactory;
    private final MessageSourceAccessor mensagens;
}
