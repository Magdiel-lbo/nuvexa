package com.nuvexa.nutricao.relatorio.service;

import com.nuvexa.core.service.OrganizacaoScopedContext;
import com.nuvexa.nutricao.model.Avaliacao;
import com.nuvexa.nutricao.model.QAvaliacao;
import com.nuvexa.nutricao.relatorio.dto.filter.AvaliacaoFiltro;
import com.nuvexa.nutricao.relatorio.dto.response.AvaliacaoRelatorioLinhaDTO;
import com.nuvexa.nutricao.service.AvaliacaoService;
import com.nuvexa.relatorios.RelatorioColuna;
import com.nuvexa.relatorios.RelatorioResponseDTO;
import com.nuvexa.relatorios.excel.RelatorioExcelWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Diferente dos demais relatórios (Consulta/Prontuário/PlanoAlimentar), a filtragem aqui não
 * acontece na query — ver o Javadoc de AvaliacaoFiltro. Por isso este service sempre carrega
 * TODAS as avaliações da organização (necessário para calcular a variação de peso de cada uma
 * contra o histórico completo do paciente) e só depois aplica o filtro em memória.
 */
@Service
@RequiredArgsConstructor
public class AvaliacaoRelatorioService {

    private final OrganizacaoScopedContext contexto;

    @Transactional(readOnly = true)
    public RelatorioResponseDTO<AvaliacaoRelatorioLinhaDTO> generate(AvaliacaoFiltro filtro) {
        return RelatorioResponseDTO.of(colunas(), linhas(filtro));
    }

    @Transactional(readOnly = true)
    public byte[] generateExcel(AvaliacaoFiltro filtro) {
        return RelatorioExcelWriter.write("Avaliações", colunas(), linhas(filtro));
    }

    private List<RelatorioColuna<AvaliacaoRelatorioLinhaDTO>> colunas() {
        return AvaliacaoRelatorioLinhaDTO.colunas(contexto.getMensagens());
    }

    private List<AvaliacaoRelatorioLinhaDTO> linhas(AvaliacaoFiltro filtro) {
        List<Avaliacao> todas = buscarTodasAvaliacoesDaOrganizacao();
        Map<Long, BigDecimal> variacaoPorId = AvaliacaoService.calcularVariacoesPorPaciente(todas);

        return todas.stream()
                .filter(avaliacao -> filtro.aceita(avaliacao, variacaoPorId.get(avaliacao.getId())))
                .map(avaliacao -> AvaliacaoRelatorioLinhaDTO.from(avaliacao, variacaoPorId.get(avaliacao.getId())))
                .toList();
    }

    private List<Avaliacao> buscarTodasAvaliacoesDaOrganizacao() {
        QAvaliacao avaliacao = QAvaliacao.avaliacao;
        return contexto.getQueryFactory()
                .selectFrom(avaliacao)
                .join(avaliacao.paciente).fetchJoin()
                .join(avaliacao.avaliador).fetchJoin()
                .where(avaliacao.organizacao.id.eq(contexto.getContextoDeAutenticacao().organizacaoAtualId()))
                .orderBy(avaliacao.data.desc())
                .fetch();
    }
}
