package com.nuvexa.core.model;

import com.nuvexa.platform.util.ComRotulo;

public enum StatusOrganizacao implements ComRotulo {
    ATIVA("Ativa"),
    SUSPENSA("Suspensa");

    private final String rotulo;

    StatusOrganizacao(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
