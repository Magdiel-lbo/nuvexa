package com.nuvexa.nutricao.report.service;

import com.nuvexa.core.contexto.ContextoDeAutenticacao;
import com.nuvexa.core.paciente.model.QPaciente;
import com.nuvexa.relatorios.RelatorioColuna;
import com.nuvexa.relatorios.RelatorioResponseDTO;
import com.nuvexa.relatorios.excel.RelatorioExcelWriter;
import com.nuvexa.nutricao.calculator.ImcCalculator;
import com.nuvexa.nutricao.calculator.GastoCaloricoCalculator;
import com.nuvexa.nutricao.calculator.TaxaMetabolicaCalculator;
import com.nuvexa.nutricao.model.PerfilNutricional;
import com.nuvexa.nutricao.model.QPerfilNutricional;
import com.nuvexa.nutricao.report.dto.request.PacienteRelatorioFiltroDTO;
import com.nuvexa.nutricao.report.dto.response.PacienteRelatorioLinhaDTO;
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
    private final ContextoDeAutenticacao contextoDeAutenticacao;
    private final MessageSource messageSource;
    private final ImcCalculator imcCalculator;
    private final TaxaMetabolicaCalculator taxaMetabolicaCalculator;
    private final GastoCaloricoCalculator gastoCaloricoCalculator;

    @Transactional(readOnly = true)
    public RelatorioResponseDTO<PacienteRelatorioLinhaDTO> generate(PacienteRelatorioFiltroDTO filtro) {
        return RelatorioResponseDTO.of(colunas(), linhas(filtro));
    }

    @Transactional(readOnly = true)
    public byte[] generateExcel(PacienteRelatorioFiltroDTO filtro) {
        return RelatorioExcelWriter.write("Pacientes", colunas(), linhas(filtro));
    }

    private List<RelatorioColuna<PacienteRelatorioLinhaDTO>> colunas() {
        return PacienteRelatorioLinhaDTO.colunas(messageSource, MESSAGE_LOCALE);
    }

    private List<PacienteRelatorioLinhaDTO> linhas(PacienteRelatorioFiltroDTO filtro) {
        return buscarPerfis(filtro).stream().map(this::toRow).toList();
    }

    private List<PerfilNutricional> buscarPerfis(PacienteRelatorioFiltroDTO filtro) {
        QPerfilNutricional perfilNutricional = QPerfilNutricional.perfilNutricional;
        QPaciente paciente = QPaciente.paciente;

        BooleanExpression filtroBusca = filtro.getBusca() == null || filtro.getBusca().isBlank()
                ? null
                : paciente.nome.containsIgnoreCase(filtro.getBusca().trim());
        BooleanExpression filtroSexo = filtro.getSexo() == null ? null : paciente.sexo.eq(filtro.getSexo());
        BooleanExpression filtroObjetivo = filtro.getObjetivo() == null ? null : perfilNutricional.objetivo.eq(filtro.getObjetivo());
        BooleanExpression filtroNivelAtividade = filtro.getNivelAtividade() == null
                ? null
                : perfilNutricional.nivelAtividade.eq(filtro.getNivelAtividade());
        // Escopo organizacional: o relatório nunca enxerga além da organização atual.
        BooleanExpression filtroOrganizacao = paciente.organizacao.id.eq(contextoDeAutenticacao.organizacaoAtualId());

        return queryFactory
                .selectFrom(perfilNutricional)
                .join(perfilNutricional.paciente, paciente).fetchJoin()
                .where(filtroOrganizacao, filtroBusca, filtroSexo, filtroObjetivo, filtroNivelAtividade)
                .orderBy(paciente.nome.asc())
                .fetch();
    }

    private PacienteRelatorioLinhaDTO toRow(PerfilNutricional perfilNutricional) {
        int idade = Period.between(perfilNutricional.getPaciente().getDataNascimento(), LocalDate.now()).getYears();
        BigDecimal imc = imcCalculator.calculate(perfilNutricional.getPeso(), perfilNutricional.getAltura());
        BigDecimal taxaMetabolicaBasal = taxaMetabolicaCalculator.calculate(
                perfilNutricional.getPeso(), perfilNutricional.getAltura(), idade, perfilNutricional.getPaciente().getSexo());
        BigDecimal gastoCaloricoDiario = perfilNutricional.getCaloriasDiariasManuais() != null
                ? perfilNutricional.getCaloriasDiariasManuais()
                : gastoCaloricoCalculator.calculate(taxaMetabolicaBasal, perfilNutricional.getNivelAtividade());

        return PacienteRelatorioLinhaDTO.from(perfilNutricional, idade, imc, imcCalculator.classify(imc), gastoCaloricoDiario);
    }
}
