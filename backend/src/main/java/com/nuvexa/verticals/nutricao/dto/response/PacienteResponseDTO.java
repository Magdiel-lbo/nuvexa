package com.nuvexa.verticals.nutricao.dto.response;

import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.verticals.nutricao.model.NivelAtividade;
import com.nuvexa.verticals.nutricao.model.Objetivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteResponseDTO {

    private Long id;
    private String nome;
    private LocalDate dataNascimento;
    private Sexo sexo;
    private BigDecimal altura;
    private BigDecimal peso;
    private Objetivo objetivo;
    private NivelAtividade nivelAtividade;
    private BigDecimal caloriasDiariasManuais;
    private String observacoes;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    private Integer idade;
    private BigDecimal imc;
    private String classificacaoImc;
    private BigDecimal taxaMetabolicaBasal;
    private BigDecimal gastoCaloricoDiario;
}
