package com.nuvexa.platform.report;

import lombok.Getter;

import java.util.function.Function;

@Getter
public class RelatorioColuna<T> {

    private final String key;
    private final String label;
    private final int order;
    private final Function<T, Object> valueExtractor;

    private RelatorioColuna(String key, String label, int order, Function<T, Object> valueExtractor) {
        this.key = key;
        this.label = label;
        this.order = order;
        this.valueExtractor = valueExtractor;
    }

    public static <T> RelatorioColuna<T> of(String key, String label, int order, Function<T, Object> valueExtractor) {
        return new RelatorioColuna<>(key, label, order, valueExtractor);
    }
}
