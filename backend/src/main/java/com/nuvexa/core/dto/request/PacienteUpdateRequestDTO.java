package com.nuvexa.core.dto.request;

import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Sexo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteUpdateRequestDTO {

    @NotBlank(message = "{paciente.nome.obrigatorio}")
    private String nome;

    @NotNull(message = "{paciente.dataNascimento.obrigatoria}")
    private LocalDate dataNascimento;

    @NotNull(message = "{paciente.sexo.obrigatorio}")
    private Sexo sexo;

    public void atualizar(Paciente paciente, ModelMapper modelMapper) {
        modelMapper.map(this, paciente);
    }
}
