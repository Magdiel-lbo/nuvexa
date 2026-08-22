package com.nuvexa.verticals.nutricao.calculator;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ImcCalculator {

    private static final Locale MESSAGE_LOCALE = Locale.of("pt", "BR");

    private final MessageSource messageSource;

    public BigDecimal calculate(BigDecimal weightKg, BigDecimal heightM) {
        BigDecimal heightSquared = heightM.multiply(heightM);
        return weightKg.divide(heightSquared, 2, RoundingMode.HALF_UP);
    }

    public String classify(BigDecimal bmi) {
        String key;
        if (bmi.compareTo(new BigDecimal("18.5")) < 0) {
            key = "imc.classificacao.abaixoPeso";
        } else if (bmi.compareTo(new BigDecimal("25.0")) < 0) {
            key = "imc.classificacao.normal";
        } else if (bmi.compareTo(new BigDecimal("30.0")) < 0) {
            key = "imc.classificacao.sobrepeso";
        } else if (bmi.compareTo(new BigDecimal("35.0")) < 0) {
            key = "imc.classificacao.obesidadeGrau1";
        } else if (bmi.compareTo(new BigDecimal("40.0")) < 0) {
            key = "imc.classificacao.obesidadeGrau2";
        } else {
            key = "imc.classificacao.obesidadeGrau3";
        }
        return messageSource.getMessage(key, null, MESSAGE_LOCALE);
    }
}
