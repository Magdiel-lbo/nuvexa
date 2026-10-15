package com.nuvexa.core.dto.request;

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
public class ConviteProprietarioRequestDTO {

    @NotBlank(message = "{autenticacao.email.obrigatorio}")
    @Email(message = "{autenticacao.email.invalido}")
    @Size(max = 150, message = "{convite.email.tamanho}")
    private String email;
}
