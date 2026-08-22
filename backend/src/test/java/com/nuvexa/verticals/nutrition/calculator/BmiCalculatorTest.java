package com.nuvexa.verticals.nutrition.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BmiCalculatorTest {

    @Mock
    private MessageSource messageSource;

    private BmiCalculator bmiCalculator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(messageSource.getMessage(any(String.class), any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        bmiCalculator = new BmiCalculator(messageSource);
    }

    @Test
    void shouldCalculateBmiFromWeightAndHeight() {
        BigDecimal bmi = bmiCalculator.calculate(new BigDecimal("80"), new BigDecimal("1.73"));

        assertThat(bmi).isEqualByComparingTo("26.73");
    }

    @ParameterizedTest
    @CsvSource({
            "17.0, bmi.classification.underweight",
            "18.4, bmi.classification.underweight",
            "18.5, bmi.classification.normal",
            "24.9, bmi.classification.normal",
            "25.0, bmi.classification.overweight",
            "29.9, bmi.classification.overweight",
            "30.0, bmi.classification.obese1",
            "34.9, bmi.classification.obese1",
            "35.0, bmi.classification.obese2",
            "39.9, bmi.classification.obese2",
            "40.0, bmi.classification.obese3",
            "50.0, bmi.classification.obese3"
    })
    void shouldClassifyBmiIntoTheCorrectRange(String bmiValue, String expectedKey) {
        String classification = bmiCalculator.classify(new BigDecimal(bmiValue));

        assertThat(classification).isEqualTo(expectedKey);
    }
}
