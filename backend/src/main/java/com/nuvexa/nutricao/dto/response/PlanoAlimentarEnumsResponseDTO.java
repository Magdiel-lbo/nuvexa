package com.nuvexa.nutricao.dto.response;

import com.nuvexa.nutricao.model.StatusPlanoAlimentar;
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
public class PlanoAlimentarEnumsResponseDTO {

    private List<EnumOpcaoDTO> status;

    public static PlanoAlimentarEnumsResponseDTO of() {
        return PlanoAlimentarEnumsResponseDTO.builder()
                .status(EnumOpcaoResolver.resolve(StatusPlanoAlimentar.class))
                .build();
    }
}
