package com.nuvexa.nutricao.dto.response;

import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.nutricao.model.PerfilNutricional;
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

    public static PacienteResponseDTO from(PerfilNutricional perfilNutricional) {
        Paciente paciente = perfilNutricional.getPaciente();
        return PacienteResponseDTO.builder()
                .id(paciente.getId())
                .nome(paciente.getNome())
                .dataNascimento(paciente.getDataNascimento())
                .sexo(paciente.getSexo())
                .altura(perfilNutricional.getAltura())
                .peso(perfilNutricional.getPeso())
                .objetivo(perfilNutricional.getObjetivo())
                .nivelAtividade(perfilNutricional.getNivelAtividade())
                .caloriasDiariasManuais(perfilNutricional.getCaloriasDiariasManuais())
                .observacoes(perfilNutricional.getObservacoes())
                .criadoEm(perfilNutricional.getCriadoEm())
                .atualizadoEm(perfilNutricional.getAtualizadoEm())
                .build();
    }
}
