package com.nuvexa.nutricao.relatorio.dto.response;

import com.nuvexa.nutricao.model.Avaliacao;
import com.nuvexa.nutricao.model.StatusAvaliacao;
import com.nuvexa.nutricao.model.TipoAvaliacao;
import com.nuvexa.relatorios.RelatorioColuna;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.support.MessageSourceAccessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoRelatorioLinhaDTO {

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Long id;
    private String pacienteNome;
    private LocalDate data;
    private TipoAvaliacao tipo;
    private StatusAvaliacao status;
    private String avaliadorNome;
    private BigDecimal peso;
    private BigDecimal percentualGordura;
    private BigDecimal variacaoPeso;

    public static AvaliacaoRelatorioLinhaDTO from(Avaliacao avaliacao, BigDecimal variacaoPeso) {
        return AvaliacaoRelatorioLinhaDTO.builder()
                .id(avaliacao.getId())
                .pacienteNome(avaliacao.getPaciente().getNome())
                .data(avaliacao.getData())
                .tipo(avaliacao.getTipo())
                .status(avaliacao.getStatus())
                .avaliadorNome(avaliacao.getAvaliador().getNome())
                .peso(avaliacao.getPeso())
                .percentualGordura(avaliacao.getPercentualGordura())
                .variacaoPeso(variacaoPeso)
                .build();
    }

    public static List<RelatorioColuna<AvaliacaoRelatorioLinhaDTO>> colunas(MessageSourceAccessor mensagens) {
        return List.of(
                coluna("paciente", "relatorio.avaliacao.coluna.paciente", 1, mensagens,
                        AvaliacaoRelatorioLinhaDTO::getPacienteNome),
                coluna("data", "relatorio.avaliacao.coluna.data", 2, mensagens,
                        row -> row.getData().format(DATA_FORMATTER)),
                coluna("tipo", "relatorio.avaliacao.coluna.tipo", 3, mensagens,
                        row -> row.getTipo().getRotulo()),
                coluna("status", "relatorio.avaliacao.coluna.status", 4, mensagens,
                        row -> row.getStatus().getRotulo()),
                coluna("peso", "relatorio.avaliacao.coluna.peso", 5, mensagens,
                        AvaliacaoRelatorioLinhaDTO::getPeso),
                coluna("percentualGordura", "relatorio.avaliacao.coluna.percentualGordura", 6, mensagens,
                        AvaliacaoRelatorioLinhaDTO::getPercentualGordura),
                coluna("variacaoPeso", "relatorio.avaliacao.coluna.variacaoPeso", 7, mensagens,
                        AvaliacaoRelatorioLinhaDTO::getVariacaoPeso),
                coluna("avaliador", "relatorio.avaliacao.coluna.avaliador", 8, mensagens,
                        AvaliacaoRelatorioLinhaDTO::getAvaliadorNome));
    }

    private static RelatorioColuna<AvaliacaoRelatorioLinhaDTO> coluna(
            String chave, String chaveMensagem, int ordem, MessageSourceAccessor mensagens,
            Function<AvaliacaoRelatorioLinhaDTO, Object> extratorValor) {
        return RelatorioColuna.of(chave, mensagens.getMessage(chaveMensagem), ordem, extratorValor);
    }
}
