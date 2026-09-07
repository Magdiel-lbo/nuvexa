package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Sexo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Paciente genérico — só os campos de {@link Paciente} (core). Dado clínico de nutrição
 * (altura/peso/objetivo/IMC...) vive em {@code PerfilNutricionalResponseDTO}, na vertical.
 */
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
    private Integer idade;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public static PacienteResponseDTO from(Paciente paciente) {
        return PacienteResponseDTO.builder()
                .id(paciente.getId())
                .nome(paciente.getNome())
                .dataNascimento(paciente.getDataNascimento())
                .sexo(paciente.getSexo())
                .idade(Period.between(paciente.getDataNascimento(), LocalDate.now()).getYears())
                .criadoEm(paciente.getCriadoEm())
                .atualizadoEm(paciente.getAtualizadoEm())
                .build();
    }
}
