package com.nuvexa.core.contexto.dto.response;

import com.nuvexa.core.identity.model.Usuario;
import com.nuvexa.core.organizacao.model.Organizacao;
import com.nuvexa.core.vinculo.model.Vinculo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Quem está autenticado, em qual organização e com qual papel. É o que o frontend usa para
 * saber o que exibir — a decisão de segurança continua sendo do backend.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContextoResponseDTO {

    private Long usuarioId;

    private String nome;

    private String email;

    /** Papel de acesso à plataforma (ADMIN/PROFISSIONAL). */
    private String perfil;

    private Long organizacaoId;

    private String organizacaoNome;

    private String organizacaoTipo;

    /** Papel dentro da organização (PROPRIETARIO/GESTOR/MEMBRO). */
    private String papelOrganizacional;

    public static ContextoResponseDTO from(Vinculo vinculo) {
        Usuario usuario = vinculo.getUsuario();
        Organizacao organizacao = vinculo.getOrganizacao();

        return ContextoResponseDTO.builder()
                .usuarioId(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .perfil(usuario.getPerfil().name())
                .organizacaoId(organizacao.getId())
                .organizacaoNome(organizacao.getNome())
                .organizacaoTipo(organizacao.getTipo().name())
                .papelOrganizacional(vinculo.getPapel().name())
                .build();
    }
}
