package com.nuvexa.core.dto.request;

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
public class RedefinirSenhaRequestDTO {

    @NotBlank(message = "{autenticacao.token.obrigatorio}")
    private String token;

    @NotBlank(message = "{autenticacao.senha.obrigatoria}")
    @Size(min = 8, message = "{autenticacao.senha.invalida}")
    private String novaSenha;
}
