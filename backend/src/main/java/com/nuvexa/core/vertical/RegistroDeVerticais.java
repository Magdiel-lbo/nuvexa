package com.nuvexa.core.vertical;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class RegistroDeVerticais {

    private final Map<Especialidade, EstrategiaDeVertical> estrategiasPorEspecialidade;

    public RegistroDeVerticais(List<EstrategiaDeVertical> estrategias) {
        Map<Especialidade, EstrategiaDeVertical> mapa = new EnumMap<>(Especialidade.class);
        for (EstrategiaDeVertical estrategia : estrategias) {
            EstrategiaDeVertical existente = mapa.putIfAbsent(estrategia.especialidade(), estrategia);
            if (existente != null) {
                throw new IllegalStateException(
                        "Mais de uma EstrategiaDeVertical declarada para a especialidade " + estrategia.especialidade());
            }
        }
        this.estrategiasPorEspecialidade = Collections.unmodifiableMap(mapa);
    }

    public Optional<EstrategiaDeVertical> buscarPorEspecialidade(Especialidade especialidade) {
        return Optional.ofNullable(estrategiasPorEspecialidade.get(especialidade));
    }

    public List<DescritorDeVertical> listarDisponiveis() {
        return estrategiasPorEspecialidade.values().stream()
                .map(EstrategiaDeVertical::descrever)
                .toList();
    }
}
