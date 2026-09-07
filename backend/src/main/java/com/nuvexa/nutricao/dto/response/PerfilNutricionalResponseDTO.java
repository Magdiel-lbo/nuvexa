package com.nuvexa.nutricao.dto.response;

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
import java.time.Period;

/**
 * {@code pacienteNome}/{@code sexo}/{@code idade} são denormalizados aqui (via
 * {@code perfilNutricional.getPaciente()}, já uma referência válida) pelo mesmo motivo de
 * {@code pacienteNome} em ConsultaResponseDTO/ProntuarioResponseDTO/etc.: conveniência de
 * exibição na listagem, sem exigir uma segunda chamada — não é o mesmo problema de core
 * depender de nutricao (aqui é nutricao lendo core.Paciente, direção permitida).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilNutricionalResponseDTO {

    private Long pacienteId;
    private String pacienteNome;
    private Sexo sexo;
    private Integer idade;
    private BigDecimal altura;
    private BigDecimal peso;
    private Long avaliacaoAtualId;
    private Objetivo objetivo;
    private NivelAtividade nivelAtividade;
    private BigDecimal caloriasDiariasManuais;
    private String observacoes;
    private BigDecimal imc;
    private String classificacaoImc;
    private BigDecimal taxaMetabolicaBasal;
    private BigDecimal gastoCaloricoDiario;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public static PerfilNutricionalResponseDTO from(
            PerfilNutricional perfilNutricional, BigDecimal peso, Long avaliacaoAtualId, BigDecimal imc,
            String classificacaoImc, BigDecimal taxaMetabolicaBasal, BigDecimal gastoCaloricoDiario) {
        return PerfilNutricionalResponseDTO.builder()
                .pacienteId(perfilNutricional.getPaciente().getId())
                .pacienteNome(perfilNutricional.getPaciente().getNome())
                .sexo(perfilNutricional.getPaciente().getSexo())
                .idade(Period.between(perfilNutricional.getPaciente().getDataNascimento(), LocalDate.now()).getYears())
                .altura(perfilNutricional.getAltura())
                .peso(peso)
                .avaliacaoAtualId(avaliacaoAtualId)
                .objetivo(perfilNutricional.getObjetivo())
                .nivelAtividade(perfilNutricional.getNivelAtividade())
                .caloriasDiariasManuais(perfilNutricional.getCaloriasDiariasManuais())
                .observacoes(perfilNutricional.getObservacoes())
                .imc(imc)
                .classificacaoImc(classificacaoImc)
                .taxaMetabolicaBasal(taxaMetabolicaBasal)
                .gastoCaloricoDiario(gastoCaloricoDiario)
                .criadoEm(perfilNutricional.getCriadoEm())
                .atualizadoEm(perfilNutricional.getAtualizadoEm())
                .build();
    }
}
