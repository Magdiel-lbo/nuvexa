package com.nuvexa.core.vertical;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class VerticalRegistry {

    private final Map<Especialidade, VerticalStrategy> estrategiasPorEspecialidade;

    public VerticalRegistry(List<VerticalStrategy> estrategias) {
        Map<Especialidade, VerticalStrategy> mapa = new EnumMap<>(Especialidade.class);
        for (VerticalStrategy estrategia : estrategias) {
            VerticalStrategy existente = mapa.putIfAbsent(estrategia.especialidade(), estrategia);
            if (existente != null) {
                throw new IllegalStateException(
                        "Mais de uma VerticalStrategy declarada para a especialidade " + estrategia.especialidade());
            }
        }
        this.estrategiasPorEspecialidade = Collections.unmodifiableMap(mapa);
    }

    public Optional<VerticalStrategy> buscarPorEspecialidade(Especialidade especialidade) {
        return Optional.ofNullable(estrategiasPorEspecialidade.get(especialidade));
    }

    public List<DescritorDeVertical> listarDisponiveis() {
        return estrategiasPorEspecialidade.values().stream()
                .map(VerticalStrategy::descrever)
                .toList();
    }
}
