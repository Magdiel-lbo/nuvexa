package com.nuvexa.nutricao.dto.request;

import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.nutricao.model.PerfilNutricional;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilNutricionalUpdateRequestDTO {

    @NotNull(message = "{paciente.altura.obrigatoria}")
    @Schema(description = "Altura em metros", example = "1.75")
    private BigDecimal altura;

    @NotNull(message = "{paciente.objetivo.obrigatorio}")
    private Objetivo objetivo;

    @NotNull(message = "{paciente.nivelAtividade.obrigatorio}")
    private NivelAtividade nivelAtividade;

    private BigDecimal caloriasDiariasManuais;

    private String observacoes;

    public void atualizar(PerfilNutricional perfilNutricional, ModelMapper modelMapper) {
        modelMapper.map(this, perfilNutricional);
    }
}
