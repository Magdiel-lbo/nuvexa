package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.Especialidade;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizacaoResponseDTO {

    private Long id;
    private String nome;
    private String razaoSocial;
    private String documento;
    private TipoOrganizacao tipo;
    private Especialidade especialidade;
    private StatusOrganizacao status;
    /** Existe ao menos um PROPRIETARIO ativo? Falso = empresa criada que ainda não foi assumida por ninguém. */
    private boolean proprietarioAtivo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public static OrganizacaoResponseDTO from(Organizacao organizacao) {
        return from(organizacao, true);
    }

    public static OrganizacaoResponseDTO from(Organizacao organizacao, boolean proprietarioAtivo) {
        return OrganizacaoResponseDTO.builder()
                .proprietarioAtivo(proprietarioAtivo)
                .id(organizacao.getId())
                .nome(organizacao.getNome())
                .razaoSocial(organizacao.getRazaoSocial())
                .documento(organizacao.getDocumento())
                .tipo(organizacao.getTipo())
                .especialidade(organizacao.getEspecialidade())
                .status(organizacao.getStatus())
                .criadoEm(organizacao.getCriadoEm())
                .atualizadoEm(organizacao.getAtualizadoEm())
                .build();
    }
}
