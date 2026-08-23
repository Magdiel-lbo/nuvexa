package com.nuvexa.core.vertical;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistroDeVerticaisTest {

    private static final DescritorDeVertical DESCRITOR_NUTRICAO =
            new DescritorDeVertical(Especialidade.NUTRICAO, "Nutrição", "/pacientes");

    @Test
    void shouldFindStrategyByEspecialidade() {
        RegistroDeVerticais registro = new RegistroDeVerticais(List.of(estrategia(Especialidade.NUTRICAO, DESCRITOR_NUTRICAO)));

        assertThat(registro.buscarPorEspecialidade(Especialidade.NUTRICAO)).isPresent();
    }

    @Test
    void shouldListAllRegisteredDescriptors() {
        RegistroDeVerticais registro = new RegistroDeVerticais(List.of(estrategia(Especialidade.NUTRICAO, DESCRITOR_NUTRICAO)));

        assertThat(registro.listarDisponiveis()).containsExactly(DESCRITOR_NUTRICAO);
    }

    @Test
    void shouldReturnEmptyWhenEspecialidadeIsNotRegistered() {
        RegistroDeVerticais registro = new RegistroDeVerticais(List.of());

        assertThat(registro.buscarPorEspecialidade(Especialidade.NUTRICAO)).isEmpty();
        assertThat(registro.listarDisponiveis()).isEmpty();
    }

    @Test
    void shouldRejectTwoStrategiesForTheSameEspecialidade() {
        List<EstrategiaDeVertical> estrategias = List.of(
                estrategia(Especialidade.NUTRICAO, DESCRITOR_NUTRICAO),
                estrategia(Especialidade.NUTRICAO, DESCRITOR_NUTRICAO));

        assertThatThrownBy(() -> new RegistroDeVerticais(estrategias))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("NUTRICAO");
    }

    private EstrategiaDeVertical estrategia(Especialidade especialidade, DescritorDeVertical descritor) {
        return new EstrategiaDeVertical() {
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
