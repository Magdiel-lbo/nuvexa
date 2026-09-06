package com.nuvexa.nutricao.dto.response;

import com.nuvexa.core.model.Sexo;
import com.nuvexa.platform.dto.EnumOpcaoDTO;
import com.nuvexa.platform.util.EnumOpcaoResolver;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteEnumsResponseDTO {

    private List<EnumOpcaoDTO> sexos;
    private List<EnumOpcaoDTO> objetivos;
    private List<EnumOpcaoDTO> niveisAtividade;

    public static PacienteEnumsResponseDTO of() {
        return PacienteEnumsResponseDTO.builder()
                .sexos(EnumOpcaoResolver.resolve(Sexo.class))
                .objetivos(EnumOpcaoResolver.resolve(Objetivo.class))
                .niveisAtividade(EnumOpcaoResolver.resolve(NivelAtividade.class))
                .build();
    }
}
