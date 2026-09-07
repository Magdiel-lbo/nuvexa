package com.nuvexa.nutricao.relatorio.service;

import com.nuvexa.core.service.OrganizacaoScopedContext;
import com.nuvexa.nutricao.model.PlanoAlimentar;
import com.nuvexa.nutricao.model.QPlanoAlimentar;
import com.nuvexa.nutricao.relatorio.dto.filter.PlanoAlimentarFiltro;
import com.nuvexa.nutricao.relatorio.dto.response.PlanoAlimentarRelatorioLinhaDTO;
import com.nuvexa.relatorios.RelatorioColuna;
import com.nuvexa.relatorios.RelatorioResponseDTO;
import com.nuvexa.relatorios.excel.RelatorioExcelWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanoAlimentarRelatorioService {

    private final OrganizacaoScopedContext contexto;

    @Transactional(readOnly = true)
    public RelatorioResponseDTO<PlanoAlimentarRelatorioLinhaDTO> generate(PlanoAlimentarFiltro filtro) {
        return RelatorioResponseDTO.of(colunas(), linhas(filtro));
    }

    @Transactional(readOnly = true)
    public byte[] generateExcel(PlanoAlimentarFiltro filtro) {
        return RelatorioExcelWriter.write("Planos alimentares", colunas(), linhas(filtro));
    }

    private List<RelatorioColuna<PlanoAlimentarRelatorioLinhaDTO>> colunas() {
        return PlanoAlimentarRelatorioLinhaDTO.colunas(contexto.getMensagens());
    }

    private List<PlanoAlimentarRelatorioLinhaDTO> linhas(PlanoAlimentarFiltro filtro) {
        return buscarPlanos(filtro).stream().map(PlanoAlimentarRelatorioLinhaDTO::from).toList();
    }

    private List<PlanoAlimentar> buscarPlanos(PlanoAlimentarFiltro filtro) {
        QPlanoAlimentar plano = QPlanoAlimentar.planoAlimentar;

        return contexto.getQueryFactory()
                .selectFrom(plano)
                .join(plano.paciente).fetchJoin()
                .join(plano.autor).fetchJoin()
                .where(filtro.toPredicate(plano, contexto.getContextoDeAutenticacao().organizacaoAtualId()))
                .orderBy(plano.atualizadoEm.desc())
                .fetch();
    }
}
