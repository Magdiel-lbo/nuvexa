package com.nuvexa.nutricao.dto.request;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.nutricao.model.PlanoAlimentar;
import com.nuvexa.nutricao.model.StatusPlanoAlimentar;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoAlimentarCreateRequestDTO {

    private Long pacienteId;

    @NotNull(message = "{planoAlimentar.autorId.obrigatorio}")
    private Long autorId;

    @NotBlank(message = "{planoAlimentar.nome.obrigatorio}")
    private String nome;

    @NotNull(message = "{planoAlimentar.dataInicio.obrigatoria}")
    private LocalDate dataInicio;

    @NotNull(message = "{planoAlimentar.calorias.obrigatorias}")
    @Positive(message = "{planoAlimentar.calorias.invalidas}")
    private Integer calorias;

    @NotNull(message = "{planoAlimentar.refeicoesPorDia.obrigatorio}")
    @Positive(message = "{planoAlimentar.refeicoesPorDia.invalido}")
    private Integer refeicoesPorDia;

    @NotNull(message = "{planoAlimentar.status.obrigatorio}")
    private StatusPlanoAlimentar status;

    public PlanoAlimentar toPlanoAlimentar(Organizacao organizacao, Paciente paciente, Usuario autor) {
        return PlanoAlimentar.builder()
                .organizacao(organizacao)
                .paciente(paciente)
                .autor(autor)
                .nome(nome)
                .dataInicio(dataInicio)
                .calorias(calorias)
                .refeicoesPorDia(refeicoesPorDia)
                .status(status)
                .build();
    }
}
