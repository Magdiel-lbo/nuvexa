package com.nuvexa.verticals.nutricao;

import com.nuvexa.core.vertical.DescritorDeVertical;
import com.nuvexa.core.vertical.Especialidade;
import com.nuvexa.core.vertical.VerticalStrategy;
import org.springframework.stereotype.Component;

@Component
public class NutricaoStrategy implements VerticalStrategy {

    @Override
    public Especialidade especialidade() {
        return Especialidade.NUTRICAO;
    }

    @Override
    public DescritorDeVertical descrever() {
        return new DescritorDeVertical(Especialidade.NUTRICAO, "Nutrição", "/pacientes");
    }
}
