package com.nuvexa.platform.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TagDescriptionCustomizer implements OpenApiCustomizer {

    private final ApiDescriptions descriptions;

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }

        Set<String> tagNames = new LinkedHashSet<>();
        openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
            if (!CollectionUtils.isEmpty(operation.getTags())) {
                tagNames.addAll(operation.getTags());
            }
        }));

        List<Tag> tags = tagNames.stream()
                .map(name -> new Tag().name(name).description(descriptions.tagDescription(name).orElse(null)))
                .toList();

        openApi.setTags(tags);
    }
}
