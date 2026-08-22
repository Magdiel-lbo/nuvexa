package com.nuvexa.verticais.nutricao.calculadora;

import com.nuvexa.verticais.nutricao.model.NivelAtividade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GastoCaloricoCalculatorTest {

    private final GastoCaloricoCalculator calculator = new GastoCaloricoCalculator();

    @ParameterizedTest
    @CsvSource({
            "SEDENTARY, 1800.00",
            "LIGHTLY_ACTIVE, 2062.50",
            "MODERATELY_ACTIVE, 2325.00",
            "VERY_ACTIVE, 2587.50",
            "EXTRA_ACTIVE, 2850.00"
    })
    void shouldApplyActivityFactorToBmr(NivelAtividade activityLevel, String expected) {
        BigDecimal result = calculator.calculate(new BigDecimal("1500"), activityLevel);

        assertThat(result).isEqualByComparingTo(expected);
    }
}
