package com.nuvexa.platform.util;

import com.nuvexa.platform.dto.EnumOptionDTO;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class EnumOptionResolver {

    private EnumOptionResolver() {
    }

    public static <E extends Enum<E>> List<EnumOptionDTO> resolve(
            Class<E> enumType, String messageKeyPrefix, MessageSource messageSource, Locale locale) {
        return Arrays.stream(enumType.getEnumConstants())
                .map(constant -> EnumOptionDTO.builder()
                        .value(constant.name())
                        .label(messageSource.getMessage(messageKeyPrefix + "." + constant.name(), null, locale))
                        .build())
                .toList();
    }
}
