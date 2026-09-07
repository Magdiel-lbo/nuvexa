package com.nuvexa.nutricao.model;

import com.nuvexa.platform.util.ComRotulo;

public enum TipoAvaliacao implements ComRotulo {
    BIOIMPEDANCIA("Bioimpedância"),
    ANTROPOMETRIA("Antropometria"),
    DOBRAS_CUTANEAS("Dobras cutâneas");

    private final String rotulo;

    TipoAvaliacao(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
