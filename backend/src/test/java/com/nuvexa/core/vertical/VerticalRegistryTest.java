package com.nuvexa.core.vertical;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerticalRegistryTest {

    private static final DescritorDeVertical DESCRITOR_NUTRICAO =
            new DescritorDeVertical(Especialidade.NUTRICAO, "Nutrição", "/pacientes");

    @Test
    void shouldFindStrategyByEspecialidade() {
        VerticalRegistry registro = new VerticalRegistry(List.of(estrategia(Especialidade.NUTRICAO, DESCRITOR_NUTRICAO)));

        assertThat(registro.buscarPorEspecialidade(Especialidade.NUTRICAO)).isPresent();
    }

    @Test
    void shouldListAllRegisteredDescriptors() {
        VerticalRegistry registro = new VerticalRegistry(List.of(estrategia(Especialidade.NUTRICAO, DESCRITOR_NUTRICAO)));

        assertThat(registro.listarDisponiveis()).containsExactly(DESCRITOR_NUTRICAO);
    }

    @Test
    void shouldReturnEmptyWhenEspecialidadeIsNotRegistered() {
        VerticalRegistry registro = new VerticalRegistry(List.of());

        assertThat(registro.buscarPorEspecialidade(Especialidade.NUTRICAO)).isEmpty();
        assertThat(registro.listarDisponiveis()).isEmpty();
    }

    @Test
    void shouldRejectTwoStrategiesForTheSameEspecialidade() {
        List<VerticalStrategy> estrategias = List.of(
                estrategia(Especialidade.NUTRICAO, DESCRITOR_NUTRICAO),
                estrategia(Especialidade.NUTRICAO, DESCRITOR_NUTRICAO));

        assertThatThrownBy(() -> new VerticalRegistry(estrategias))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("NUTRICAO");
    }

    private VerticalStrategy estrategia(Especialidade especialidade, DescritorDeVertical descritor) {
        return new VerticalStrategy() {
            @Override
            public Especialidade especialidade() {
                return especialidade;
            }

            @Override
            public DescritorDeVertical descrever() {
                return descritor;
            }
        };
    }
}
