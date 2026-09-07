package com.nuvexa.core.relatorio.service;

import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.QProntuario;
import com.nuvexa.core.relatorio.dto.filter.ProntuarioFiltro;
import com.nuvexa.core.relatorio.dto.response.ProntuarioRelatorioLinhaDTO;
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
public class ProntuarioRelatorioService {

    private final OrganizacaoScopedContext contexto;

    @Transactional(readOnly = true)
    public RelatorioResponseDTO<ProntuarioRelatorioLinhaDTO> generate(ProntuarioFiltro filtro) {
        return RelatorioResponseDTO.of(colunas(), linhas(filtro));
    }

    @Transactional(readOnly = true)
    public byte[] generateExcel(ProntuarioFiltro filtro) {
        return RelatorioExcelWriter.write("Prontuários", colunas(), linhas(filtro));
    }

    private List<RelatorioColuna<ProntuarioRelatorioLinhaDTO>> colunas() {
        return ProntuarioRelatorioLinhaDTO.colunas(contexto.getMensagens());
    }

    private List<ProntuarioRelatorioLinhaDTO> linhas(ProntuarioFiltro filtro) {
        return buscarProntuarios(filtro).stream().map(ProntuarioRelatorioLinhaDTO::from).toList();
    }

    private List<Prontuario> buscarProntuarios(ProntuarioFiltro filtro) {
        QProntuario prontuario = QProntuario.prontuario;

        return contexto.getQueryFactory()
                .selectFrom(prontuario)
                .join(prontuario.paciente).fetchJoin()
                .join(prontuario.autor).fetchJoin()
                .where(filtro.toPredicate(prontuario, contexto.getContextoDeAutenticacao().organizacaoAtualId()))
                .orderBy(prontuario.atualizadoEm.desc())
                .fetch();
    }
}
