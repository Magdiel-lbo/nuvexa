package com.nuvexa.core.dto.request;

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
public class PacienteProfissionalCreateRequestDTO {

    @NotNull(message = "{pacienteProfissional.pacienteId.obrigatorio}")
    private Long pacienteId;

    @NotNull(message = "{pacienteProfissional.profissionalId.obrigatorio}")
    private Long profissionalId;
}
