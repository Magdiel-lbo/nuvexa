package com.nuvexa.nutricao.model;

import com.nuvexa.platform.util.ComRotulo;

public enum NivelAtividade implements ComRotulo {
    SEDENTARIO("Sedentário"),
    LEVEMENTE_ATIVO("Levemente ativo"),
    MODERADAMENTE_ATIVO("Moderadamente ativo"),
    MUITO_ATIVO("Muito ativo"),
    EXTREMAMENTE_ATIVO("Extremamente ativo");

    private final String rotulo;

    NivelAtividade(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
