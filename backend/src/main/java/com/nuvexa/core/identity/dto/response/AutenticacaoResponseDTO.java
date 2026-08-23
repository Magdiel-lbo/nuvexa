package com.nuvexa.core.identity.dto.response;

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
public class AutenticacaoResponseDTO {

    private String token;

    private String tipoToken;

    private String perfil;
}
