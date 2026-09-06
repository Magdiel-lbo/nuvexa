package com.nuvexa.core.report.service;

import com.nuvexa.core.model.Consulta;
import com.nuvexa.core.model.QConsulta;
import com.nuvexa.core.report.dto.filter.ConsultaFiltro;
import com.nuvexa.core.report.dto.response.ConsultaRelatorioLinhaDTO;
import com.nuvexa.core.service.ContextoDeAutenticacao;
import com.nuvexa.relatorios.RelatorioColuna;
import com.nuvexa.relatorios.RelatorioResponseDTO;
import com.nuvexa.relatorios.excel.RelatorioExcelWriter;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ConsultaRelatorioService {

    private static final Locale MESSAGE_LOCALE = Locale.of("pt", "BR");

    private final JPAQueryFactory queryFactory;
    private final ContextoDeAutenticacao contextoDeAutenticacao;
    private final MessageSource messageSource;

    @Transactional(readOnly = true)
    public RelatorioResponseDTO<ConsultaRelatorioLinhaDTO> generate(ConsultaFiltro filtro) {
        return RelatorioResponseDTO.of(colunas(), linhas(filtro));
    }

    @Transactional(readOnly = true)
    public byte[] generateExcel(ConsultaFiltro filtro) {
        return RelatorioExcelWriter.write("Consultas", colunas(), linhas(filtro));
    }

    private List<RelatorioColuna<ConsultaRelatorioLinhaDTO>> colunas() {
        return ConsultaRelatorioLinhaDTO.colunas(messageSource, MESSAGE_LOCALE);
    }

    private List<ConsultaRelatorioLinhaDTO> linhas(ConsultaFiltro filtro) {
        return buscarConsultas(filtro).stream().map(ConsultaRelatorioLinhaDTO::from).toList();
    }

    private List<Consulta> buscarConsultas(ConsultaFiltro filtro) {
        QConsulta consulta = QConsulta.consulta;

        return queryFactory
                .selectFrom(consulta)
                .join(consulta.paciente).fetchJoin()
                .join(consulta.profissional).fetchJoin()
                .where(filtro.toPredicate(consulta, contextoDeAutenticacao.organizacaoAtualId()))
                .orderBy(consulta.dataHora.desc())
                .fetch();
    }
}
