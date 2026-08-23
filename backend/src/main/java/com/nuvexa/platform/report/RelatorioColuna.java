package com.nuvexa.platform.report;

import lombok.Getter;

import java.util.function.Function;

@Getter
public class RelatorioColuna<T> {

    private final String chave;
    private final String rotulo;
    private final int ordem;
    private final Function<T, Object> extratorValor;

    private RelatorioColuna(String chave, String rotulo, int ordem, Function<T, Object> extratorValor) {
        this.chave = chave;
        this.rotulo = rotulo;
        this.ordem = ordem;
        this.extratorValor = extratorValor;
    }

    public static <T> RelatorioColuna<T> of(String chave, String rotulo, int ordem, Function<T, Object> extratorValor) {
        return new RelatorioColuna<>(chave, rotulo, ordem, extratorValor);
    }
}
