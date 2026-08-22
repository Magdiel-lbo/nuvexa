package com.nuvexa.verticals.nutrition.calculator;

import com.nuvexa.verticals.nutrition.model.ActivityLevel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Gasto calórico diário = TMB × fator do nível de atividade (fatores de Harris-Benedict/TDEE).
 */
@Component
public class CalorieExpenditureCalculator {

    private static final Map<ActivityLevel, BigDecimal> ACTIVITY_FACTORS = Map.of(
            ActivityLevel.SEDENTARY, new BigDecimal("1.2"),
            ActivityLevel.LIGHTLY_ACTIVE, new BigDecimal("1.375"),
            ActivityLevel.MODERATELY_ACTIVE, new BigDecimal("1.55"),
            ActivityLevel.VERY_ACTIVE, new BigDecimal("1.725"),
            ActivityLevel.EXTRA_ACTIVE, new BigDecimal("1.9")
    );

    public BigDecimal calculate(BigDecimal bmr, ActivityLevel activityLevel) {
        BigDecimal factor = ACTIVITY_FACTORS.get(activityLevel);
        return bmr.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
