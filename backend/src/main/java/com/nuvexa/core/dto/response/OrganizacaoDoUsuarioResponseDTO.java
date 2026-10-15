package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.Vinculo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Uma organização em que o usuário autenticado tem vínculo ativo, com o papel dele ali. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizacaoDoUsuarioResponseDTO {

    private Long organizacaoId;
    private String nome;
    private String tipo;
    private String especialidade;
    private String papel;

    public static OrganizacaoDoUsuarioResponseDTO from(Vinculo vinculo) {
        return OrganizacaoDoUsuarioResponseDTO.builder()
                .organizacaoId(vinculo.getOrganizacao().getId())
                .nome(vinculo.getOrganizacao().getNome())
                .tipo(vinculo.getOrganizacao().getTipo().name())
                .especialidade(vinculo.getOrganizacao().getEspecialidade().name())
                .papel(vinculo.getPapel().name())
                .build();
    }
}
