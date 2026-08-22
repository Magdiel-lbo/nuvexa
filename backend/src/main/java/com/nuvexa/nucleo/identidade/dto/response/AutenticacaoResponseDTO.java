package com.nuvexa.nucleo.identidade.dto.response;

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

    private String tokenType;

    private String role;
}
