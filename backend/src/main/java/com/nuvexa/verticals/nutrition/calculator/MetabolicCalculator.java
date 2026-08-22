package com.nuvexa.verticals.nutrition.calculator;

import com.nuvexa.core.patient.model.Gender;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Taxa metabólica basal (TMB) pela fórmula de Mifflin-St Jeor.
 */
@Component
public class MetabolicCalculator {

    private static final BigDecimal CM_PER_METER = new BigDecimal("100");
    private static final BigDecimal WEIGHT_FACTOR = new BigDecimal("10");
    private static final BigDecimal HEIGHT_FACTOR = new BigDecimal("6.25");
    private static final BigDecimal AGE_FACTOR = new BigDecimal("5");
    private static final BigDecimal MALE_CONSTANT = new BigDecimal("5");
    private static final BigDecimal FEMALE_CONSTANT = new BigDecimal("-161");

    public BigDecimal calculate(BigDecimal weightKg, BigDecimal heightM, int age, Gender gender) {
        BigDecimal heightCm = heightM.multiply(CM_PER_METER);
        BigDecimal genderConstant = gender == Gender.MALE ? MALE_CONSTANT : FEMALE_CONSTANT;

        BigDecimal result = weightKg.multiply(WEIGHT_FACTOR)
                .add(heightCm.multiply(HEIGHT_FACTOR))
                .subtract(BigDecimal.valueOf(age).multiply(AGE_FACTOR))
                .add(genderConstant);

        return result.setScale(2, RoundingMode.HALF_UP);
    }
}
