package com.nuvexa.core.dto.request;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.ProntuarioAdendo;
import com.nuvexa.core.model.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProntuarioAdendoCreateRequestDTO {

    @NotNull(message = "{prontuario.autorId.obrigatorio}")
    private Long autorId;

    @NotBlank(message = "{prontuarioAdendo.texto.obrigatorio}")
    private String texto;

    public ProntuarioAdendo toProntuarioAdendo(Organizacao organizacao, Prontuario prontuario, Usuario autor) {
        return ProntuarioAdendo.builder()
                .organizacao(organizacao)
                .prontuario(prontuario)
                .autor(autor)
                .texto(texto)
                .build();
    }
}
