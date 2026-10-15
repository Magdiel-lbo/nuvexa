package com.nuvexa.core.dto.request;

import com.nuvexa.core.model.PapelOrganizacional;
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
public class MembroPapelRequestDTO {

    @NotNull(message = "{convite.papel.obrigatorio}")
    private PapelOrganizacional papel;
}
