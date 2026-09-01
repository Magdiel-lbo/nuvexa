package com.nuvexa.core.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class EsqueciSenhaRequestDTO {

    @NotBlank(message = "{autenticacao.email.obrigatorio}")
    @Email(message = "{autenticacao.email.invalido}")
    private String email;
}
