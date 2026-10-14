package com.nuvexa.core.dto.request;

import com.nuvexa.core.model.Especialidade;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizacaoCreateRequestDTO {

    @NotBlank(message = "{organizacao.nome.obrigatorio}")
    @Size(max = 150, message = "{organizacao.nome.tamanho}")
    private String nome;

    @Size(max = 150, message = "{organizacao.razaoSocial.tamanho}")
    private String razaoSocial;

    @Size(max = 20, message = "{organizacao.documento.tamanho}")
    private String documento;

    @NotNull(message = "{organizacao.tipo.obrigatorio}")
    private TipoOrganizacao tipo;

    @NotNull(message = "{organizacao.especialidade.obrigatoria}")
    private Especialidade especialidade;

    public Organizacao toOrganizacao(String razaoSocialNormalizada, String documentoNormalizado) {
        return Organizacao.builder()
                .nome(nome.trim())
                .razaoSocial(razaoSocialNormalizada)
                .documento(documentoNormalizado)
                .tipo(tipo)
                .especialidade(especialidade)
                .status(StatusOrganizacao.ATIVA)
                .build();
    }
}
