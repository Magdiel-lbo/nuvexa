package com.nuvexa.core.model;

import com.nuvexa.platform.util.ComRotulo;

public enum SecaoProntuario implements ComRotulo {
    ANAMNESE("Anamnese"),
    EVOLUCAO("Evolução"),
    EXAMES("Exames");

    private final String rotulo;

    SecaoProntuario(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
