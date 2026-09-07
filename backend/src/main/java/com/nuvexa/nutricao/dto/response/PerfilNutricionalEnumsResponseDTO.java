package com.nuvexa.nutricao.dto.response;

import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.platform.dto.EnumOpcaoDTO;
import com.nuvexa.platform.util.EnumOpcaoResolver;
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
public class PerfilNutricionalEnumsResponseDTO {

    private List<EnumOpcaoDTO> objetivos;
    private List<EnumOpcaoDTO> niveisAtividade;

    public static PerfilNutricionalEnumsResponseDTO of() {
        return PerfilNutricionalEnumsResponseDTO.builder()
                .objetivos(EnumOpcaoResolver.resolve(Objetivo.class))
                .niveisAtividade(EnumOpcaoResolver.resolve(NivelAtividade.class))
                .build();
    }
}
