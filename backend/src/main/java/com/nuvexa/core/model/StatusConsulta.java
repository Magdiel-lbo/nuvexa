package com.nuvexa.core.model;

import com.nuvexa.platform.util.ComRotulo;

public enum StatusConsulta implements ComRotulo {
    AGENDADA("Agendada"),
    CONFIRMADA("Confirmada"),
    REALIZADA("Realizada"),
    CANCELADA("Cancelada"),
    FALTOU("Faltou");

    private final String rotulo;

    StatusConsulta(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
