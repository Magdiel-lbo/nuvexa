package com.nuvexa.nutricao.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.MessageSourceAccessor;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ImcCalculatorTest {

    @Mock
    private MessageSourceAccessor mensagens;

    private ImcCalculator imcCalculator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mensagens.getMessage(any(String.class))).thenAnswer(invocation -> invocation.getArgument(0));
        imcCalculator = new ImcCalculator(mensagens);
    }

    @Test
    void shouldCalculateBmiFromWeightAndHeight() {
        BigDecimal bmi = imcCalculator.calculate(new BigDecimal("80"), new BigDecimal("1.73"));

        assertThat(bmi).isEqualByComparingTo("26.73");
    }

    @ParameterizedTest
    @CsvSource({
            "17.0, imc.classificacao.abaixoPeso",
            "18.4, imc.classificacao.abaixoPeso",
            "18.5, imc.classificacao.normal",
            "24.9, imc.classificacao.normal",
            "25.0, imc.classificacao.sobrepeso",
            "29.9, imc.classificacao.sobrepeso",
            "30.0, imc.classificacao.obesidadeGrau1",
            "34.9, imc.classificacao.obesidadeGrau1",
            "35.0, imc.classificacao.obesidadeGrau2",
            "39.9, imc.classificacao.obesidadeGrau2",
            "40.0, imc.classificacao.obesidadeGrau3",
            "50.0, imc.classificacao.obesidadeGrau3"
    })
    void shouldClassifyBmiIntoTheCorrectRange(String bmiValue, String expectedKey) {
        String classification = imcCalculator.classify(new BigDecimal(bmiValue));

        assertThat(classification).isEqualTo(expectedKey);
    }
}
