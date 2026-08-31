package com.nuvexa.nutricao.calculator;

import com.nuvexa.nutricao.model.NivelAtividade;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Gasto calórico diário = TMB × fator do nível de atividade (fatores de Harris-Benedict/TDEE).
 */
@Component
public class GastoCaloricoCalculator {

    private static final Map<NivelAtividade, BigDecimal> FATORES_ATIVIDADE = Map.of(
            NivelAtividade.SEDENTARIO, new BigDecimal("1.2"),
            NivelAtividade.LEVEMENTE_ATIVO, new BigDecimal("1.375"),
            NivelAtividade.MODERADAMENTE_ATIVO, new BigDecimal("1.55"),
            NivelAtividade.MUITO_ATIVO, new BigDecimal("1.725"),
            NivelAtividade.EXTREMAMENTE_ATIVO, new BigDecimal("1.9")
    );

    public BigDecimal calculate(BigDecimal taxaMetabolicaBasal, NivelAtividade nivelAtividade) {
        BigDecimal fator = FATORES_ATIVIDADE.get(nivelAtividade);
        return taxaMetabolicaBasal.multiply(fator).setScale(2, RoundingMode.HALF_UP);
    }
}
