package com.nuvexa.core.dto.request;

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
public class RefreshRequestDTO {

    @NotBlank(message = "{autenticacao.token.obrigatorio}")
    private String refreshToken;
}
