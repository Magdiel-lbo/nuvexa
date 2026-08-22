package com.nuvexa.verticals.nutrition.dto.response;

import com.nuvexa.platform.dto.EnumOptionDTO;
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
public class PatientEnumsResponseDTO {

    private List<EnumOptionDTO> genders;
    private List<EnumOptionDTO> goals;
    private List<EnumOptionDTO> activityLevels;
}
