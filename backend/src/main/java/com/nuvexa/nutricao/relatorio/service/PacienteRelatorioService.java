package com.nuvexa.nutricao.relatorio.service;

import com.nuvexa.core.service.OrganizacaoScopedContext;
import com.nuvexa.core.model.QPaciente;
import com.nuvexa.relatorios.RelatorioColuna;
import com.nuvexa.relatorios.RelatorioResponseDTO;
import com.nuvexa.relatorios.excel.RelatorioExcelWriter;
import com.nuvexa.nutricao.calculator.ImcCalculator;
import com.nuvexa.nutricao.calculator.GastoCaloricoCalculator;
import com.nuvexa.nutricao.calculator.TaxaMetabolicaCalculator;
import com.nuvexa.nutricao.model.PerfilNutricional;
import com.nuvexa.nutricao.model.QPerfilNutricional;
import com.nuvexa.nutricao.relatorio.dto.filter.PacienteFiltro;
import com.nuvexa.nutricao.relatorio.dto.response.PacienteRelatorioLinhaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PacienteRelatorioService {

    private final ImcCalculator imcCalculator;
    private final TaxaMetabolicaCalculator taxaMetabolicaCalculator;
    private final GastoCaloricoCalculator gastoCaloricoCalculator;
    private final OrganizacaoScopedContext contexto;

    @Transactional(readOnly = true)
    public RelatorioResponseDTO<PacienteRelatorioLinhaDTO> generate(PacienteFiltro filtro) {
        return RelatorioResponseDTO.of(colunas(), linhas(filtro));
    }

    @Transactional(readOnly = true)
    public byte[] generateExcel(PacienteFiltro filtro) {
        return RelatorioExcelWriter.write("Pacientes", colunas(), linhas(filtro));
    }

    private List<RelatorioColuna<PacienteRelatorioLinhaDTO>> colunas() {
        return PacienteRelatorioLinhaDTO.colunas(contexto.getMensagens());
    }

    private List<PacienteRelatorioLinhaDTO> linhas(PacienteFiltro filtro) {
        return buscarPerfis(filtro).stream().map(this::toRow).toList();
    }

    private List<PerfilNutricional> buscarPerfis(PacienteFiltro filtro) {
        QPerfilNutricional perfilNutricional = QPerfilNutricional.perfilNutricional;
        QPaciente paciente = QPaciente.paciente;

        return contexto.getQueryFactory()
                .selectFrom(perfilNutricional)
                .join(perfilNutricional.paciente, paciente).fetchJoin()
                .where(filtro.toPredicate(paciente, perfilNutricional, contexto.getContextoDeAutenticacao().organizacaoAtualId()))
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
