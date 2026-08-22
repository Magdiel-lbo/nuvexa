package com.nuvexa.platform.report;

import lombok.Getter;

import java.util.function.Function;

@Getter
public class ReportColumn<T> {

    private final String key;
    private final String label;
    private final int order;
    private final Function<T, Object> valueExtractor;

    private ReportColumn(String key, String label, int order, Function<T, Object> valueExtractor) {
        this.key = key;
        this.label = label;
        this.order = order;
        this.valueExtractor = valueExtractor;
    }

    public static <T> ReportColumn<T> of(String key, String label, int order, Function<T, Object> valueExtractor) {
        return new ReportColumn<>(key, label, order, valueExtractor);
    }
}
