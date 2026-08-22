package com.nuvexa.verticals.nutrition.calculator;

import com.nuvexa.verticals.nutrition.model.ActivityLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CalorieExpenditureCalculatorTest {

    private final CalorieExpenditureCalculator calculator = new CalorieExpenditureCalculator();

    @ParameterizedTest
    @CsvSource({
            "SEDENTARY, 1800.00",
            "LIGHTLY_ACTIVE, 2062.50",
            "MODERATELY_ACTIVE, 2325.00",
            "VERY_ACTIVE, 2587.50",
            "EXTRA_ACTIVE, 2850.00"
    })
    void shouldApplyActivityFactorToBmr(ActivityLevel activityLevel, String expected) {
        BigDecimal result = calculator.calculate(new BigDecimal("1500"), activityLevel);

        assertThat(result).isEqualByComparingTo(expected);
    }
}
