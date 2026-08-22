package com.nuvexa.verticais.nutricao.relatorio.service;

import com.nuvexa.core.paciente.model.QPaciente;
import com.nuvexa.plataforma.relatorio.RelatorioColuna;
import com.nuvexa.plataforma.relatorio.RelatorioResponseDTO;
import com.nuvexa.plataforma.relatorio.excel.RelatorioExcelWriter;
import com.nuvexa.verticais.nutricao.calculadora.ImcCalculator;
import com.nuvexa.verticais.nutricao.calculadora.GastoCaloricoCalculator;
import com.nuvexa.verticais.nutricao.calculadora.TaxaMetabolicaCalculator;
import com.nuvexa.verticais.nutricao.model.PerfilNutricional;
import com.nuvexa.verticais.nutricao.model.QPerfilNutricional;
import com.nuvexa.verticais.nutricao.relatorio.dto.request.PacienteRelatorioFiltroDTO;
import com.nuvexa.verticais.nutricao.relatorio.dto.response.PacienteRelatorioLinhaDTO;
import com.nuvexa.verticais.nutricao.relatorio.mapper.PacienteRelatorioMapper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PacienteRelatorioService {

    private static final Locale MESSAGE_LOCALE = Locale.of("pt", "BR");

    private final JPAQueryFactory queryFactory;
    private final MessageSource messageSource;
    private final PacienteRelatorioMapper pacienteRelatorioMapper;
    private final ImcCalculator imcCalculator;
    private final TaxaMetabolicaCalculator taxaMetabolicaCalculator;
    private final GastoCaloricoCalculator gastoCaloricoCalculator;

    @Transactional(readOnly = true)
    public RelatorioResponseDTO<PacienteRelatorioLinhaDTO> generate(PacienteRelatorioFiltroDTO filter) {
        return RelatorioResponseDTO.of(columns(), rows(filter));
    }

    @Transactional(readOnly = true)
    public byte[] generateExcel(PacienteRelatorioFiltroDTO filter) {
        return RelatorioExcelWriter.write("Pacientes", columns(), rows(filter));
    }

    private List<RelatorioColuna<PacienteRelatorioLinhaDTO>> columns() {
        return pacienteRelatorioMapper.buildColumns(messageSource, MESSAGE_LOCALE);
    }

    private List<PacienteRelatorioLinhaDTO> rows(PacienteRelatorioFiltroDTO filter) {
        return searchProfiles(filter).stream().map(this::toRow).toList();
    }

    private List<PerfilNutricional> searchProfiles(PacienteRelatorioFiltroDTO filter) {
        QPerfilNutricional perfilNutricional = QPerfilNutricional.perfilNutricional;
        QPaciente paciente = QPaciente.paciente;

        BooleanExpression searchFilter = filter.getSearch() == null || filter.getSearch().isBlank()
                ? null
                : paciente.nome.containsIgnoreCase(filter.getSearch().trim());
        BooleanExpression genderFilter = filter.getGender() == null ? null : paciente.sexo.eq(filter.getGender());
        BooleanExpression goalFilter = filter.getGoal() == null ? null : perfilNutricional.objetivo.eq(filter.getGoal());
        BooleanExpression activityLevelFilter = filter.getActivityLevel() == null
                ? null
                : perfilNutricional.nivelAtividade.eq(filter.getActivityLevel());

        return queryFactory
                .selectFrom(perfilNutricional)
                .join(perfilNutricional.paciente, paciente)
                .where(searchFilter, genderFilter, goalFilter, activityLevelFilter)
                .orderBy(paciente.nome.asc())
                .fetch();
    }

    private PacienteRelatorioLinhaDTO toRow(PerfilNutricional perfilNutricional) {
        int age = Period.between(perfilNutricional.getPaciente().getDataNascimento(), LocalDate.now()).getYears();
        BigDecimal bmi = imcCalculator.calculate(perfilNutricional.getPeso(), perfilNutricional.getAltura());
        BigDecimal bmr = taxaMetabolicaCalculator.calculate(
                perfilNutricional.getPeso(), perfilNutricional.getAltura(), age, perfilNutricional.getPaciente().getSexo());
        BigDecimal dailyCalorieExpenditure = perfilNutricional.getCaloriasDiariasManuais() != null
                ? perfilNutricional.getCaloriasDiariasManuais()
                : gastoCaloricoCalculator.calculate(bmr, perfilNutricional.getNivelAtividade());

        return pacienteRelatorioMapper.toRow(perfilNutricional, age, bmi, imcCalculator.classify(bmi), dailyCalorieExpenditure);
    }
}
