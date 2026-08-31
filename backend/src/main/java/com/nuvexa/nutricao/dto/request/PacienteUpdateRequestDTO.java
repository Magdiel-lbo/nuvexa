package com.nuvexa.nutricao.dto.request;

import com.nuvexa.core.paciente.model.Paciente;
import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.nutricao.model.PerfilNutricional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
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

    @NotNull(message = "{paciente.altura.obrigatoria}")
    private BigDecimal altura;

    @NotNull(message = "{paciente.peso.obrigatorio}")
    private BigDecimal peso;

    @NotNull(message = "{paciente.objetivo.obrigatorio}")
    private Objetivo objetivo;

    @NotNull(message = "{paciente.nivelAtividade.obrigatorio}")
    private NivelAtividade nivelAtividade;

    private BigDecimal caloriasDiariasManuais;

    private String observacoes;

    public void atualizar(Paciente paciente, ModelMapper modelMapper) {
        modelMapper.map(this, paciente);
    }

    public void atualizar(PerfilNutricional perfilNutricional, ModelMapper modelMapper) {
        modelMapper.map(this, perfilNutricional);
    }
}
