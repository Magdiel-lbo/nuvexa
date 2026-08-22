package com.nuvexa.verticais.nutricao.calculadora;

import com.nuvexa.verticais.nutricao.model.NivelAtividade;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Gasto calórico diário = TMB × fator do nível de atividade (fatores de Harris-Benedict/TDEE).
 */
@Component
public class GastoCaloricoCalculator {

    private static final Map<NivelAtividade, BigDecimal> ACTIVITY_FACTORS = Map.of(
            NivelAtividade.SEDENTARY, new BigDecimal("1.2"),
            NivelAtividade.LIGHTLY_ACTIVE, new BigDecimal("1.375"),
            NivelAtividade.MODERATELY_ACTIVE, new BigDecimal("1.55"),
            NivelAtividade.VERY_ACTIVE, new BigDecimal("1.725"),
            NivelAtividade.EXTRA_ACTIVE, new BigDecimal("1.9")
    );

    public BigDecimal calculate(BigDecimal bmr, NivelAtividade activityLevel) {
        BigDecimal factor = ACTIVITY_FACTORS.get(activityLevel);
        return bmr.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
