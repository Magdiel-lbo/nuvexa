package com.nuvexa.core.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tipo e especialidade não mudam depois da criação (há dado de negócio já escopado por eles). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizacaoUpdateRequestDTO {

    @NotBlank(message = "{organizacao.nome.obrigatorio}")
    @Size(max = 150, message = "{organizacao.nome.tamanho}")
    private String nome;

    @Size(max = 150, message = "{organizacao.razaoSocial.tamanho}")
    private String razaoSocial;

    @Size(max = 20, message = "{organizacao.documento.tamanho}")
    private String documento;
}
