package com.nuvexa.platform.util;

import com.nuvexa.platform.dto.EnumOpcaoDTO;

import java.util.Arrays;
import java.util.List;

public final class EnumOpcaoResolver {

    private EnumOpcaoResolver() {
    }

    public static <E extends Enum<E> & ComRotulo> List<EnumOpcaoDTO> resolve(Class<E> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .map(constant -> EnumOpcaoDTO.builder()
                        .valor(constant.name())
                        .rotulo(constant.getRotulo())
                        .build())
                .toList();
    }
}
