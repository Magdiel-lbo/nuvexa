package com.nuvexa.verticals.nutricao.calculator;

import com.nuvexa.verticals.nutricao.model.NivelAtividade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GastoCaloricoCalculatorTest {

    private final GastoCaloricoCalculator calculator = new GastoCaloricoCalculator();

    @ParameterizedTest
    @CsvSource({
            "SEDENTARIO, 1800.00",
            "LEVEMENTE_ATIVO, 2062.50",
            "MODERADAMENTE_ATIVO, 2325.00",
            "MUITO_ATIVO, 2587.50",
            "EXTREMAMENTE_ATIVO, 2850.00"
    })
    void shouldApplyActivityFactorToBmr(NivelAtividade nivelAtividade, String expected) {
        BigDecimal result = calculator.calculate(new BigDecimal("1500"), nivelAtividade);

        assertThat(result).isEqualByComparingTo(expected);
    }
}
