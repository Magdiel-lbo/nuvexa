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
public class ProfissionalResponseDTO {

    private Long id;

    private String nome;

    public static ProfissionalResponseDTO from(Usuario usuario) {
        return ProfissionalResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .build();
    }
}
