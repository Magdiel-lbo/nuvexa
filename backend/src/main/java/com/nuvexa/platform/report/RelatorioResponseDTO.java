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

    private List<RelatorioColunaDTO> colunas;
    private List<T> linhas;

    public static <T> RelatorioResponseDTO<T> of(List<RelatorioColuna<T>> colunas, List<T> linhas) {
        List<RelatorioColunaDTO> colunasDTO = colunas.stream()
                .sorted(Comparator.comparingInt(RelatorioColuna::getOrdem))
                .map(coluna -> RelatorioColunaDTO.builder()
                        .chave(coluna.getChave())
                        .rotulo(coluna.getRotulo())
                        .ordem(coluna.getOrdem())
                        .build())
                .toList();
        return new RelatorioResponseDTO<>(colunasDTO, linhas);
    }
}
