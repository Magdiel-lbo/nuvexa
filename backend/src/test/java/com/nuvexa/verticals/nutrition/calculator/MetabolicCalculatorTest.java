package com.nuvexa.verticals.nutrition.calculator;

import com.nuvexa.core.patient.model.Gender;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MetabolicCalculatorTest {

    private final MetabolicCalculator calculator = new MetabolicCalculator();

    @Test
    void shouldCalculateBmrForMale() {
        // Mifflin-St Jeor: 10*80 + 6.25*180 - 5*30 + 5 = 1780
        BigDecimal bmr = calculator.calculate(new BigDecimal("80"), new BigDecimal("1.80"), 30, Gender.MALE);

        assertThat(bmr).isEqualByComparingTo("1780.00");
    }

    @Test
    void shouldCalculateBmrForFemale() {
        // Mifflin-St Jeor: 10*62.5 + 6.25*165 - 5*25 - 161 = 1370.25
        BigDecimal bmr = calculator.calculate(new BigDecimal("62.5"), new BigDecimal("1.65"), 25, Gender.FEMALE);

        assertThat(bmr).isEqualByComparingTo("1370.25");
    }
}
