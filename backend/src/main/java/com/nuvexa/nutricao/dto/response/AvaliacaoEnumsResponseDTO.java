package com.nuvexa.nutricao.dto.response;

import com.nuvexa.nutricao.model.StatusAvaliacao;
import com.nuvexa.nutricao.model.TipoAvaliacao;
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
public class AvaliacaoEnumsResponseDTO {

    private List<EnumOpcaoDTO> tipos;
    private List<EnumOpcaoDTO> status;

    public static AvaliacaoEnumsResponseDTO of() {
        return AvaliacaoEnumsResponseDTO.builder()
                .tipos(EnumOpcaoResolver.resolve(TipoAvaliacao.class))
                .status(EnumOpcaoResolver.resolve(StatusAvaliacao.class))
                .build();
    }
}
