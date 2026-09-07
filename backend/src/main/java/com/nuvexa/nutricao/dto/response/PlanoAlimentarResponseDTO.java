package com.nuvexa.nutricao.dto.response;

import com.nuvexa.nutricao.model.PlanoAlimentar;
import com.nuvexa.nutricao.model.StatusPlanoAlimentar;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoAlimentarResponseDTO {

    private Long id;

    private Long pacienteId;

    private String pacienteNome;

    private Long autorId;

    private String autorNome;

    private String nome;

    private LocalDate dataInicio;

    private Integer calorias;

    private Integer refeicoesPorDia;

    private StatusPlanoAlimentar status;

    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    public static PlanoAlimentarResponseDTO from(PlanoAlimentar planoAlimentar) {
        return PlanoAlimentarResponseDTO.builder()
                .id(planoAlimentar.getId())
                .pacienteId(planoAlimentar.getPaciente().getId())
                .pacienteNome(planoAlimentar.getPaciente().getNome())
                .autorId(planoAlimentar.getAutor().getId())
                .autorNome(planoAlimentar.getAutor().getNome())
                .nome(planoAlimentar.getNome())
                .dataInicio(planoAlimentar.getDataInicio())
                .calorias(planoAlimentar.getCalorias())
                .refeicoesPorDia(planoAlimentar.getRefeicoesPorDia())
                .status(planoAlimentar.getStatus())
                .criadoEm(planoAlimentar.getCriadoEm())
                .atualizadoEm(planoAlimentar.getAtualizadoEm())
                .build();
    }
}
