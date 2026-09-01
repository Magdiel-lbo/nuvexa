package com.nuvexa.nutricao.calculator;

import com.nuvexa.core.model.Sexo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TaxaMetabolicaCalculatorTest {

    private final TaxaMetabolicaCalculator calculator = new TaxaMetabolicaCalculator();

    @Test
    void shouldCalculateBmrForMale() {
        // Mifflin-St Jeor: 10*80 + 6.25*180 - 5*30 + 5 = 1780
        BigDecimal bmr = calculator.calculate(new BigDecimal("80"), new BigDecimal("1.80"), 30, Sexo.MASCULINO);

        assertThat(bmr).isEqualByComparingTo("1780.00");
    }

    @Test
    void shouldCalculateBmrForFemale() {
        // Mifflin-St Jeor: 10*62.5 + 6.25*165 - 5*25 - 161 = 1370.25
        BigDecimal bmr = calculator.calculate(new BigDecimal("62.5"), new BigDecimal("1.65"), 25, Sexo.FEMININO);

        assertThat(bmr).isEqualByComparingTo("1370.25");
    }
}
