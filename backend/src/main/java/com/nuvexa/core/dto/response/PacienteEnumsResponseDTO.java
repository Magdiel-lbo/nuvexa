package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.Sexo;
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
public class PacienteEnumsResponseDTO {

    private List<EnumOpcaoDTO> sexos;

    public static PacienteEnumsResponseDTO of() {
        return PacienteEnumsResponseDTO.builder()
                .sexos(EnumOpcaoResolver.resolve(Sexo.class))
                .build();
    }
}
