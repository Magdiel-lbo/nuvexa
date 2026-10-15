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
public class ProfissionalVinculoRequestDTO {

    @NotNull(message = "{pacienteProfissional.profissionalId.obrigatorio}")
    private Long profissionalId;
}
