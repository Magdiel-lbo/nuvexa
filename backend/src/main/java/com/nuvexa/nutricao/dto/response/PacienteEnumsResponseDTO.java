package com.nuvexa.nutricao.dto.response;

import com.nuvexa.core.model.Sexo;
import com.nuvexa.platform.dto.EnumOpcaoDTO;
import com.nuvexa.platform.util.EnumOpcaoResolver;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteEnumsResponseDTO {

    private List<EnumOpcaoDTO> sexos;
    private List<EnumOpcaoDTO> objetivos;
    private List<EnumOpcaoDTO> niveisAtividade;

    public static PacienteEnumsResponseDTO of(MessageSource messageSource, Locale locale) {
        return PacienteEnumsResponseDTO.builder()
                .sexos(EnumOpcaoResolver.resolve(Sexo.class, "enum.sexo", messageSource, locale))
                .objetivos(EnumOpcaoResolver.resolve(Objetivo.class, "enum.objetivo", messageSource, locale))
                .niveisAtividade(EnumOpcaoResolver.resolve(NivelAtividade.class, "enum.nivelAtividade", messageSource, locale))
                .build();
    }
}
