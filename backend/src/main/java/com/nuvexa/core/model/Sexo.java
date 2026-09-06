package com.nuvexa.core.model;

import com.nuvexa.platform.util.ComRotulo;

public enum Sexo implements ComRotulo {
    MASCULINO("Masculino"),
    FEMININO("Feminino");

    private final String rotulo;

    Sexo(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
