package com.nuvexa.platform.web;

import com.nuvexa.platform.exception.ApiErro;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.GlobalOperationComponentsCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import java.util.List;
import java.util.Map;

/**
 * Aplica tag/summary/description/parâmetros/respostas de ApiDescriptions a cada operação, para
 * que os controllers não precisem de @Operation/@ApiResponses/@Parameter/@Content/@Schema.
 * Só preenche o que ainda não existe (description em branco, response code ausente), então
 * funciona como default e não como sobrescrita forçada de uma anotação manual pontual.
 *
 * Implementa GlobalOperationComponentsCustomizer (não o OperationCustomizer simples) porque
 * precisamos de Components para registrar/referenciar schemas de erro (ApiErro) sem duplicá-los
 * — springdoc só entrega essa referência nessa variante (confirmado no bytecode de
 * AbstractOpenApiResource: só chama a versão com Components quando o bean é dessa interface).
 */
@Component
@RequiredArgsConstructor
public class OperationDescriptionCustomizer implements GlobalOperationComponentsCustomizer {

    /**
     * Schemas de erro que o descriptions.yml pode referenciar por nome (campo "schema"). Uma
     * string arbitrária vinda de YAML não vira Class via reflection mágica — precisa estar
     * cadastrada aqui explicitamente.
     */
    private static final Map<String, Class<?>> KNOWN_SCHEMAS = Map.of("ApiErro", ApiErro.class);

    private final ApiDescriptions descriptions;

    @Override
    public Operation customize(Operation operation, Components components, HandlerMethod handlerMethod) {
        Class<?> controllerClass = handlerMethod.getBeanType();

        descriptions.forController(controllerClass).ifPresent(controllerDoc -> {
            if (CollectionUtils.isEmpty(operation.getTags()) && StringUtils.hasText(controllerDoc.tag())) {
                operation.setTags(List.of(controllerDoc.tag()));
            }
        });

        descriptions.forEndpoint(controllerClass, operationKey(handlerMethod)).ifPresent(endpointDoc -> {
            if (!StringUtils.hasText(operation.getSummary()) && StringUtils.hasText(endpointDoc.summary())) {
                operation.setSummary(endpointDoc.summary());
            }
            if (!StringUtils.hasText(operation.getDescription()) && StringUtils.hasText(endpointDoc.description())) {
                operation.setDescription(endpointDoc.description());
            }
            applyParameterDescriptions(operation, endpointDoc);
            applyResponses(operation, components, endpointDoc);
        });

        return operation;
    }

    /**
     * Só chamado se algum outro ponto do springdoc um dia invocar a assinatura de 2 argumentos
     * de OperationCustomizer diretamente; no fluxo normal desta versão, springdoc detecta que
     * este bean é GlobalOperationComponentsCustomizer e usa sempre a variante com Components.
     */
    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        return customize(operation, new Components(), handlerMethod);
    }

    private static void applyParameterDescriptions(Operation operation, ApiDescriptions.EndpointDescription endpointDoc) {
        if (CollectionUtils.isEmpty(operation.getParameters()) || endpointDoc.parameters().isEmpty()) {
            return;
        }
        operation.getParameters().forEach(parameter -> {
            ApiDescriptions.ParameterDescription paramDoc = endpointDoc.parameters().get(parameter.getName());
            if (paramDoc != null && !StringUtils.hasText(parameter.getDescription()) && StringUtils.hasText(paramDoc.description())) {
                parameter.setDescription(paramDoc.description());
            }
        });
    }

    private void applyResponses(Operation operation, Components components, ApiDescriptions.EndpointDescription endpointDoc) {
        if (endpointDoc.responses().isEmpty()) {
            return;
        }
        if (operation.getResponses() == null) {
            operation.setResponses(new ApiResponses());
        }

        endpointDoc.responses().forEach((code, responseDoc) -> {
            ApiResponse existing = operation.getResponses().get(code);
            if (existing != null) {
                // Resposta já sintetizada pelo Springdoc a partir de @ResponseStatus, com uma
                // descrição-padrão não vazia ("OK", "Created", "No Content") — sempre
                // sobrescrevemos com o texto do YAML, que é a fonte de verdade para descrições.
                if (StringUtils.hasText(responseDoc.description())) {
                    existing.setDescription(responseDoc.description());
                }
                return;
            }

            ApiResponse response = new ApiResponse().description(responseDoc.description());
            if (StringUtils.hasText(responseDoc.schema())) {
                response.setContent(errorContent(components, responseDoc.schema()));
            }
            operation.getResponses().addApiResponse(code, response);
        });
    }

    private Content errorContent(Components components, String schemaName) {
        registerSchemaIfAbsent(components, schemaName);
        return new Content().addMediaType("*/*", new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + schemaName)));
    }

    private void registerSchemaIfAbsent(Components components, String schemaName) {
        if (components.getSchemas() != null && components.getSchemas().containsKey(schemaName)) {
            return;
        }
        Class<?> schemaClass = KNOWN_SCHEMAS.get(schemaName);
        if (schemaClass == null) {
            throw new IllegalStateException(
                    "Schema '" + schemaName + "' referenciado em descriptions.yml não está em OperationDescriptionCustomizer.KNOWN_SCHEMAS");
        }
        ResolvedSchema resolved = ModelConverters.getInstance().resolveAsResolvedSchema(new AnnotatedType(schemaClass));
        components.addSchemas(schemaName, resolved.schema);
        resolved.referencedSchemas.forEach(components::addSchemas);
    }

    private static String operationKey(HandlerMethod handlerMethod) {
        RequestMapping classMapping = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequestMapping.class);
        RequestMapping methodMapping = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RequestMapping.class);

        String classPath = classMapping != null && classMapping.value().length > 0 ? classMapping.value()[0] : "";
        String methodPath = methodMapping != null && methodMapping.value().length > 0 ? methodMapping.value()[0] : "";
        String httpMethod = methodMapping != null && methodMapping.method().length > 0
                ? methodMapping.method()[0].name()
                : "GET";

        String path = (classPath + methodPath).replaceAll("//+", "/");
        return httpMethod + " " + (path.isEmpty() ? "/" : path);
    }
}
