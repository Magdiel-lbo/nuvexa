package com.nuvexa.nutricao.calculator;

import com.nuvexa.core.model.Sexo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Taxa metabólica basal (TMB) pela fórmula de Mifflin-St Jeor.
 */
@Component
public class TaxaMetabolicaCalculator {

    private static final BigDecimal CM_POR_METRO = new BigDecimal("100");
    private static final BigDecimal FATOR_PESO = new BigDecimal("10");
    private static final BigDecimal FATOR_ALTURA = new BigDecimal("6.25");
    private static final BigDecimal FATOR_IDADE = new BigDecimal("5");
    private static final BigDecimal CONSTANTE_MASCULINO = new BigDecimal("5");
    private static final BigDecimal CONSTANTE_FEMININO = new BigDecimal("-161");

    public BigDecimal calculate(BigDecimal pesoKg, BigDecimal alturaM, int idade, Sexo sexo) {
        BigDecimal alturaCm = alturaM.multiply(CM_POR_METRO);
        BigDecimal constanteSexo = sexo == Sexo.MASCULINO ? CONSTANTE_MASCULINO : CONSTANTE_FEMININO;

        BigDecimal resultado = pesoKg.multiply(FATOR_PESO)
                .add(alturaCm.multiply(FATOR_ALTURA))
                .subtract(BigDecimal.valueOf(idade).multiply(FATOR_IDADE))
                .add(constanteSexo);

        return resultado.setScale(2, RoundingMode.HALF_UP);
    }
}
