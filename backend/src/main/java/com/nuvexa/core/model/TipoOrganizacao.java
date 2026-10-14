package com.nuvexa.core.model;

import com.nuvexa.platform.util.ComRotulo;

public enum TipoOrganizacao implements ComRotulo {
    INDIVIDUAL("Individual"),
    CLINICA("Clínica");

    private final String rotulo;

    TipoOrganizacao(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
