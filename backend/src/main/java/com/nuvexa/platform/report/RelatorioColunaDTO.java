package com.nuvexa.platform.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatorioColunaDTO {

    private String chave;
    private String rotulo;
    private Integer ordem;
}
