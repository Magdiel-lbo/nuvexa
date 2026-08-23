package com.nuvexa.core.vertical;

/**
 * Descreve uma vertical de negócio (ex.: Nutrição, Psicologia) para o {@link RegistroDeVerticais}.
 * Não executa regra de negócio — isso continua em Controller/Service/Repository da própria vertical.
 */
public interface EstrategiaDeVertical {

    Especialidade especialidade();

    DescritorDeVertical descrever();
}
