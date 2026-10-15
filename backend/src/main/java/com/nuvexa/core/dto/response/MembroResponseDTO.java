package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.PapelOrganizacional;
import com.nuvexa.core.model.Vinculo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Membro da organização atual: o vínculo, não o usuário (o mesmo usuário pode ter outros vínculos). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembroResponseDTO {

    private Long vinculoId;
    private Long usuarioId;
    private String nome;
    private String email;
    private PapelOrganizacional papel;
    private boolean ativo;
    private LocalDateTime desde;

    public static MembroResponseDTO from(Vinculo vinculo) {
        return MembroResponseDTO.builder()
                .vinculoId(vinculo.getId())
                .usuarioId(vinculo.getUsuario().getId())
                .nome(vinculo.getUsuario().getNome())
                .email(vinculo.getUsuario().getEmail())
                .papel(vinculo.getPapel())
                .ativo(vinculo.isAtivo())
                .desde(vinculo.getCriadoEm())
                .build();
    }
}
