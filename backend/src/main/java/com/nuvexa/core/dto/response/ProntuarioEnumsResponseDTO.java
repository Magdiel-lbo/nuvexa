package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.SecaoProntuario;
import com.nuvexa.core.model.StatusProntuario;
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
public class ProntuarioEnumsResponseDTO {

    private List<EnumOpcaoDTO> secoes;
    private List<EnumOpcaoDTO> status;

    public static ProntuarioEnumsResponseDTO of() {
        return ProntuarioEnumsResponseDTO.builder()
                .secoes(EnumOpcaoResolver.resolve(SecaoProntuario.class))
                .status(EnumOpcaoResolver.resolve(StatusProntuario.class))
                .build();
    }
}
