package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.Convite;
import com.nuvexa.core.model.PapelOrganizacional;
import com.nuvexa.core.model.StatusConvite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Nunca inclui o token nem o hash dele. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConviteResponseDTO {

    private Long id;
    private String email;
    private PapelOrganizacional papel;
    private StatusConvite status;
    private LocalDateTime expiraEm;
    private String convidadoPorNome;
    private LocalDateTime criadoEm;

    public static ConviteResponseDTO from(Convite convite) {
        return ConviteResponseDTO.builder()
                .id(convite.getId())
                .email(convite.getEmail())
                .papel(convite.getPapel())
                .status(convite.status(LocalDateTime.now()))
                .expiraEm(convite.getExpiraEm())
                .convidadoPorNome(convite.getConvidadoPor().getNome())
                .criadoEm(convite.getCriadoEm())
                .build();
    }
}
