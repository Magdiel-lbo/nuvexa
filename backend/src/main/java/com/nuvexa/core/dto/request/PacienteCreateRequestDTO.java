package com.nuvexa.core.dto.request;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Sexo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PacienteCreateRequestDTO {

    @NotBlank(message = "{paciente.nome.obrigatorio}")
    private String nome;

    @NotNull(message = "{paciente.dataNascimento.obrigatoria}")
    private LocalDate dataNascimento;

    @NotNull(message = "{paciente.sexo.obrigatorio}")
    private Sexo sexo;

    public Paciente toPaciente(Organizacao organizacao) {
        return Paciente.builder()
                .organizacao(organizacao)
                .nome(nome)
                .dataNascimento(dataNascimento)
                .sexo(sexo)
                .build();
    }
}
