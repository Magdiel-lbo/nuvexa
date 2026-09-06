package com.nuvexa.nutricao.model;

import com.nuvexa.platform.util.ComRotulo;

public enum Objetivo implements ComRotulo {
    EMAGRECIMENTO("Emagrecimento"),
    MANUTENCAO_PESO("Manutenção do peso"),
    GANHO_MASSA_MUSCULAR("Ganho de massa muscular"),
    CONDICIONAMENTO_FISICO("Condicionamento físico");

    private final String rotulo;

    Objetivo(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
