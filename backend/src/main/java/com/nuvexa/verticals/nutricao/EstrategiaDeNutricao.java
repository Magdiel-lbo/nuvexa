package com.nuvexa.verticals.nutricao;

import com.nuvexa.core.vertical.DescritorDeVertical;
import com.nuvexa.core.vertical.Especialidade;
import com.nuvexa.core.vertical.EstrategiaDeVertical;
import org.springframework.stereotype.Component;

@Component
public class EstrategiaDeNutricao implements EstrategiaDeVertical {

    @Override
    public Especialidade especialidade() {
        return Especialidade.NUTRICAO;
    }

    @Override
    public DescritorDeVertical descrever() {
        return new DescritorDeVertical(Especialidade.NUTRICAO, "Nutrição", "/pacientes");
    }
}
