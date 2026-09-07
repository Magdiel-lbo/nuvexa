package com.nuvexa.nutricao.model;

import com.nuvexa.platform.util.ComRotulo;

public enum StatusPlanoAlimentar implements ComRotulo {
    ATIVO("Ativo"),
    RASCUNHO("Rascunho"),
    ENCERRADO("Encerrado");

    private final String rotulo;

    StatusPlanoAlimentar(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
