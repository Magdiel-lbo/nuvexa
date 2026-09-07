package com.nuvexa.nutricao.model;

import com.nuvexa.platform.util.ComRotulo;

public enum StatusAvaliacao implements ComRotulo {
    AGENDADA("Agendada"),
    CONCLUIDA("Concluída");

    private final String rotulo;

    StatusAvaliacao(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
