package com.nuvexa.core.dto.request;

import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class CadastroRequestDTO {

    @NotBlank(message = "{autenticacao.nome.obrigatorio}")
    private String nome;

    @NotBlank(message = "{autenticacao.email.obrigatorio}")
    @Email(message = "{autenticacao.email.invalido}")
    private String email;

    @NotBlank(message = "{autenticacao.senha.obrigatoria}")
    @Size(min = 8, message = "{autenticacao.senha.invalida}")
    private String senha;

    public Usuario toUsuario(String senhaCriptografada, Perfil perfil, boolean ativo) {
        return Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(senhaCriptografada)
                .perfil(perfil)
                .ativo(ativo)
                .build();
    }
}
