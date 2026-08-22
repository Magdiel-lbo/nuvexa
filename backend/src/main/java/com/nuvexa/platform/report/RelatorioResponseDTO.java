package com.nuvexa.platform.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioResponseDTO<T> {

    private List<RelatorioColunaDTO> columns;
    private List<T> rows;

    public static <T> RelatorioResponseDTO<T> of(List<RelatorioColuna<T>> columns, List<T> rows) {
        List<RelatorioColunaDTO> columnDTOs = columns.stream()
                .sorted(Comparator.comparingInt(RelatorioColuna::getOrder))
                .map(column -> RelatorioColunaDTO.builder()
                        .key(column.getKey())
                        .label(column.getLabel())
                        .order(column.getOrder())
                        .build())
                .toList();
        return new RelatorioResponseDTO<>(columnDTOs, rows);
    }
}
