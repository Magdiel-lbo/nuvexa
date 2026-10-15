package com.nuvexa.core.model;

import com.nuvexa.platform.util.ComRotulo;

public enum StatusConvite implements ComRotulo {
    PENDENTE("Pendente"),
    ACEITO("Aceito"),
    EXPIRADO("Expirado"),
    REVOGADO("Revogado");

    private final String rotulo;

    StatusConvite(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
