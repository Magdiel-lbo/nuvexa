package com.nuvexa.core.relatorio.service;

import com.nuvexa.core.model.Consulta;
import com.nuvexa.core.model.QConsulta;
import com.nuvexa.core.relatorio.dto.filter.ConsultaFiltro;
import com.nuvexa.core.relatorio.dto.response.ConsultaRelatorioLinhaDTO;
import com.nuvexa.core.service.OrganizacaoScopedContext;
import com.nuvexa.relatorios.RelatorioColuna;
import com.nuvexa.relatorios.RelatorioResponseDTO;
import com.nuvexa.relatorios.excel.RelatorioExcelWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultaRelatorioService {

    private final OrganizacaoScopedContext contexto;

    @Transactional(readOnly = true)
    public RelatorioResponseDTO<ConsultaRelatorioLinhaDTO> generate(ConsultaFiltro filtro) {
        return RelatorioResponseDTO.of(colunas(), linhas(filtro));
    }

    @Transactional(readOnly = true)
    public byte[] generateExcel(ConsultaFiltro filtro) {
        return RelatorioExcelWriter.write("Consultas", colunas(), linhas(filtro));
    }

    private List<RelatorioColuna<ConsultaRelatorioLinhaDTO>> colunas() {
        return ConsultaRelatorioLinhaDTO.colunas(contexto.getMensagens());
    }

    private List<ConsultaRelatorioLinhaDTO> linhas(ConsultaFiltro filtro) {
        return buscarConsultas(filtro).stream().map(ConsultaRelatorioLinhaDTO::from).toList();
    }

    private List<Consulta> buscarConsultas(ConsultaFiltro filtro) {
        QConsulta consulta = QConsulta.consulta;

        return contexto.getQueryFactory()
                .selectFrom(consulta)
                .join(consulta.paciente).fetchJoin()
                .join(consulta.profissional).fetchJoin()
                .where(filtro.toPredicate(consulta, contexto.getContextoDeAutenticacao().organizacaoAtualId()))
                .orderBy(consulta.dataHora.desc())
                .fetch();
    }
}
