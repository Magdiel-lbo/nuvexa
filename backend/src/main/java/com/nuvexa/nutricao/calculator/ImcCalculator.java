package com.nuvexa.nutricao.calculator;

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

    public BigDecimal calculate(BigDecimal pesoKg, BigDecimal alturaM) {
        BigDecimal alturaAoQuadrado = alturaM.multiply(alturaM);
        return pesoKg.divide(alturaAoQuadrado, 2, RoundingMode.HALF_UP);
    }

    public String classify(BigDecimal imc) {
        String chave;
        if (imc.compareTo(new BigDecimal("18.5")) < 0) {
            chave = "imc.classificacao.abaixoPeso";
        } else if (imc.compareTo(new BigDecimal("25.0")) < 0) {
            chave = "imc.classificacao.normal";
        } else if (imc.compareTo(new BigDecimal("30.0")) < 0) {
            chave = "imc.classificacao.sobrepeso";
        } else if (imc.compareTo(new BigDecimal("35.0")) < 0) {
            chave = "imc.classificacao.obesidadeGrau1";
        } else if (imc.compareTo(new BigDecimal("40.0")) < 0) {
            chave = "imc.classificacao.obesidadeGrau2";
        } else {
            chave = "imc.classificacao.obesidadeGrau3";
        }
        return messageSource.getMessage(chave, null, MESSAGE_LOCALE);
    }
}
