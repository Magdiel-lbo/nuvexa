package com.nuvexa.verticals.nutricao;

import com.nuvexa.core.vertical.DescritorDeVertical;
import com.nuvexa.core.vertical.Especialidade;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NutricaoStrategyTest {

    private final NutricaoStrategy estrategia = new NutricaoStrategy();

    @Test
    void shouldDeclareNutricaoEspecialidade() {
        assertThat(estrategia.especialidade()).isEqualTo(Especialidade.NUTRICAO);
    }

    @Test
    void shouldDescreverNutricaoVertical() {
        DescritorDeVertical descritor = estrategia.descrever();

        assertThat(descritor.especialidade()).isEqualTo(Especialidade.NUTRICAO);
        assertThat(descritor.nome()).isEqualTo("Nutrição");
        assertThat(descritor.rotaBase()).isEqualTo("/pacientes");
    }
}
