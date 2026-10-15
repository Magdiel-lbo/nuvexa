package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.Usuario;
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

    private String refreshToken;

    private long expiraEmSegundos;

    private String tipoToken;

    private String perfil;

    public static AutenticacaoResponseDTO of(String token, String refreshToken, long expiraEmSegundos, Usuario usuario) {
        return AutenticacaoResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiraEmSegundos(expiraEmSegundos)
                .tipoToken("Bearer")
                .perfil(usuario.getPerfil().name())
                .build();
    }
}
