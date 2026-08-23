package com.nuvexa.platform.util;

import com.nuvexa.platform.dto.EnumOpcaoDTO;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class EnumOpcaoResolver {

    private EnumOpcaoResolver() {
    }

    public static <E extends Enum<E>> List<EnumOpcaoDTO> resolve(
            Class<E> enumType, String messageKeyPrefix, MessageSource messageSource, Locale locale) {
        return Arrays.stream(enumType.getEnumConstants())
                .map(constant -> EnumOpcaoDTO.builder()
                        .valor(constant.name())
                        .rotulo(messageSource.getMessage(messageKeyPrefix + "." + constant.name(), null, locale))
                        .build())
                .toList();
    }
}
