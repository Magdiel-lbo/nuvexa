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
public class ReportResponseDTO<T> {

    private List<ReportColumnDTO> columns;
    private List<T> rows;

    public static <T> ReportResponseDTO<T> of(List<ReportColumn<T>> columns, List<T> rows) {
        List<ReportColumnDTO> columnDTOs = columns.stream()
                .sorted(Comparator.comparingInt(ReportColumn::getOrder))
                .map(column -> ReportColumnDTO.builder()
                        .key(column.getKey())
                        .label(column.getLabel())
                        .order(column.getOrder())
                        .build())
                .toList();
        return new ReportResponseDTO<>(columnDTOs, rows);
    }
}
