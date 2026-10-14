package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.Especialidade;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
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
public class OrganizacaoEnumsResponseDTO {

    private List<EnumOpcaoDTO> tipos;
    private List<EnumOpcaoDTO> especialidades;
    private List<EnumOpcaoDTO> status;

    public static OrganizacaoEnumsResponseDTO of() {
        return OrganizacaoEnumsResponseDTO.builder()
                .tipos(EnumOpcaoResolver.resolve(TipoOrganizacao.class))
                .especialidades(EnumOpcaoResolver.resolve(Especialidade.class))
                .status(EnumOpcaoResolver.resolve(StatusOrganizacao.class))
                .build();
    }
}
