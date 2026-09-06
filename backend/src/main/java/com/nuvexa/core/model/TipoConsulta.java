package com.nuvexa.core.model;

import com.nuvexa.platform.util.ComRotulo;

public enum TipoConsulta implements ComRotulo {
    PRIMEIRA_CONSULTA("Primeira consulta"),
    RETORNO("Retorno"),
    AVALIACAO("Avaliação");

    private final String rotulo;

    TipoConsulta(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
