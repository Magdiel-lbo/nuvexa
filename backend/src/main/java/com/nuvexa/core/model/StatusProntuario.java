package com.nuvexa.core.model;

import com.nuvexa.platform.util.ComRotulo;

public enum StatusProntuario implements ComRotulo {
    RASCUNHO("Rascunho"),
    PENDENTE("Pendente"),
    ASSINADO("Assinado");

    private final String rotulo;

    StatusProntuario(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
