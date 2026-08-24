package com.nuvexa.core.vertical;

/**
 * Descreve uma vertical de negócio (ex.: Nutrição, Psicologia) para o {@link VerticalRegistry}.
 * Não executa regra de negócio — isso continua em Controller/Service/Repository da própria vertical.
 */
public interface VerticalStrategy {

    Especialidade especialidade();

    DescritorDeVertical descrever();
}
