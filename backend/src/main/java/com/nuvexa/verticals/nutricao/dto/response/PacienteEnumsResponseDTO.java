package com.nuvexa.verticals.nutricao.dto.response;

import com.nuvexa.platform.dto.EnumOpcaoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteEnumsResponseDTO {

    private List<EnumOpcaoDTO> genders;
    private List<EnumOpcaoDTO> goals;
    private List<EnumOpcaoDTO> activityLevels;
}
