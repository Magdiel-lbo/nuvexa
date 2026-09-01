package com.nuvexa.platform.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Textos humanos (tag/summary/description/parâmetros/respostas) do Swagger, carregados de
 * classpath:openapi/descriptions.yml — mantidos fora dos controllers.
 *
 * A tag é localizada por "SimpleName do @RestController" (um por controller, sem ambiguidade
 * possível). Cada endpoint é localizado, dentro do bloco do controller, por "HTTP_METHOD path"
 * (ex.: "GET /api/v1/pacientes/{id}") — a identidade real da rota no Spring/OpenAPI, montada por
 * OperationDescriptionCustomizer a partir do @RequestMapping efetivo do método. Diferente de
 * usar o nome do método Java, essa chave não colide em caso de sobrecarga (duas rotas nunca têm
 * o mesmo verbo+path) e não fica acoplada ao nome escolhido para o método.
 */
@Component
public class ApiDescriptions {

    public record ParameterDescription(String description) {
    }

    public record ResponseDescription(String description, String schema) {
    }

    public record EndpointDescription(String summary, String description,
                                       Map<String, ParameterDescription> parameters,
                                       Map<String, ResponseDescription> responses) {
    }

    public record ControllerDescription(String tag, String tagDescription, Map<String, EndpointDescription> endpoints) {
    }

    private final Map<String, ControllerDescription> byController = new HashMap<>();
    private final Map<String, String> tagDescriptionByTagName = new HashMap<>();

    public ApiDescriptions() {
        try (InputStream in = new ClassPathResource("openapi/descriptions.yml").getInputStream()) {
            Map<String, Object> raw = new Yaml().load(in);
            if (raw != null) {
                raw.forEach(this::parseController);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao carregar openapi/descriptions.yml", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void parseController(String controllerName, Object value) {
        Map<String, Object> controllerMap = (Map<String, Object>) value;
        String tag = (String) controllerMap.get("tag");
        String tagDescription = (String) controllerMap.get("tagDescription");

        Map<String, EndpointDescription> endpoints = new HashMap<>();
        Map<String, Object> endpointsMap = (Map<String, Object>) controllerMap.getOrDefault("endpoints", Map.of());
        endpointsMap.forEach((operationKey, endpointValue) -> endpoints.put(operationKey, parseEndpoint((Map<String, Object>) endpointValue)));

        byController.put(controllerName, new ControllerDescription(tag, tagDescription, endpoints));
        if (tag != null) {
            tagDescriptionByTagName.put(tag, tagDescription);
        }
    }

    @SuppressWarnings("unchecked")
    private EndpointDescription parseEndpoint(Map<String, Object> endpointMap) {
        Map<String, ParameterDescription> parameters = new HashMap<>();
        Map<String, Object> parametersMap = (Map<String, Object>) endpointMap.getOrDefault("parameters", Map.of());
        parametersMap.forEach((paramName, paramValue) -> {
            Map<String, Object> paramMap = (Map<String, Object>) paramValue;
            parameters.put(paramName, new ParameterDescription((String) paramMap.get("description")));
        });

        Map<String, ResponseDescription> responses = new HashMap<>();
        Map<String, Object> responsesMap = (Map<String, Object>) endpointMap.getOrDefault("responses", Map.of());
        responsesMap.forEach((code, responseValue) -> {
            Map<String, Object> responseMap = (Map<String, Object>) responseValue;
            responses.put(code, new ResponseDescription((String) responseMap.get("description"), (String) responseMap.get("schema")));
        });

        return new EndpointDescription((String) endpointMap.get("summary"), (String) endpointMap.get("description"), parameters, responses);
    }

    public Optional<ControllerDescription> forController(Class<?> controllerClass) {
        return Optional.ofNullable(byController.get(controllerClass.getSimpleName()));
    }

    public Optional<EndpointDescription> forEndpoint(Class<?> controllerClass, String operationKey) {
        return forController(controllerClass)
                .map(ControllerDescription::endpoints)
                .map(endpoints -> endpoints.get(operationKey));
    }

    public Optional<String> tagDescription(String tagName) {
        return Optional.ofNullable(tagDescriptionByTagName.get(tagName));
    }
}
