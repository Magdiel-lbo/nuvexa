package com.nuvexa.verticais.nutricao.relatorio.mapper;

import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.core.paciente.model.Paciente;
import com.nuvexa.plataforma.dto.EnumOpcaoDTO;
import com.nuvexa.plataforma.relatorio.RelatorioColuna;
import com.nuvexa.plataforma.util.EnumOpcaoResolver;
import com.nuvexa.verticais.nutricao.model.NivelAtividade;
import com.nuvexa.verticais.nutricao.model.Objetivo;
import com.nuvexa.verticais.nutricao.model.PerfilNutricional;
import com.nuvexa.verticais.nutricao.relatorio.dto.response.PacienteRelatorioLinhaDTO;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PacienteRelatorioMapper {

    public List<RelatorioColuna<PacienteRelatorioLinhaDTO>> buildColumns(MessageSource messageSource, Locale locale) {
        Map<String, String> genderLabels = labelMap(EnumOpcaoResolver.resolve(Sexo.class, "enum.sexo", messageSource, locale));
        Map<String, String> goalLabels = labelMap(EnumOpcaoResolver.resolve(Objetivo.class, "enum.objetivo", messageSource, locale));
        Map<String, String> activityLevelLabels = labelMap(
                EnumOpcaoResolver.resolve(NivelAtividade.class, "enum.nivelAtividade", messageSource, locale));

        return List.of(
                column("name", "relatorio.paciente.coluna.nome", 1, messageSource, locale, PacienteRelatorioLinhaDTO::getName),
                column("age", "relatorio.paciente.coluna.idade", 2, messageSource, locale, PacienteRelatorioLinhaDTO::getAge),
                column("gender", "relatorio.paciente.coluna.sexo", 3, messageSource, locale,
                        row -> genderLabels.get(row.getGender().name())),
                column("goal", "relatorio.paciente.coluna.objetivo", 4, messageSource, locale,
                        row -> goalLabels.get(row.getGoal().name())),
                column("activityLevel", "relatorio.paciente.coluna.nivelAtividade", 5, messageSource, locale,
                        row -> activityLevelLabels.get(row.getActivityLevel().name())),
                column("bmi", "relatorio.paciente.coluna.imc", 6, messageSource, locale, PacienteRelatorioLinhaDTO::getBmi),
                column("bmiClassification", "relatorio.paciente.coluna.classificacaoImc", 7, messageSource, locale,
                        PacienteRelatorioLinhaDTO::getBmiClassification),
                column("dailyCalorieExpenditure", "relatorio.paciente.coluna.gastoCaloricoDiario", 8, messageSource, locale,
                        PacienteRelatorioLinhaDTO::getDailyCalorieExpenditure));
    }

    public PacienteRelatorioLinhaDTO toRow(
            PerfilNutricional perfilNutricional, int age, BigDecimal bmi, String bmiClassification, BigDecimal dailyCalorieExpenditure) {
        Paciente paciente = perfilNutricional.getPaciente();
        return PacienteRelatorioLinhaDTO.builder()
                .id(paciente.getId())
                .name(paciente.getNome())
                .age(age)
                .gender(paciente.getSexo())
                .goal(perfilNutricional.getObjetivo())
                .activityLevel(perfilNutricional.getNivelAtividade())
                .bmi(bmi)
                .bmiClassification(bmiClassification)
                .dailyCalorieExpenditure(dailyCalorieExpenditure)
                .build();
    }

    private Map<String, String> labelMap(List<EnumOpcaoDTO> options) {
        return options.stream().collect(Collectors.toMap(EnumOpcaoDTO::getValue, EnumOpcaoDTO::getLabel));
    }

    private RelatorioColuna<PacienteRelatorioLinhaDTO> column(
            String key, String messageKey, int order, MessageSource messageSource, Locale locale,
            Function<PacienteRelatorioLinhaDTO, Object> valueExtractor) {
        return RelatorioColuna.of(key, messageSource.getMessage(messageKey, null, locale), order, valueExtractor);
    }
}
