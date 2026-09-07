package com.nuvexa.nutricao.relatorio.dto.response;

import com.nuvexa.nutricao.model.PlanoAlimentar;
import com.nuvexa.nutricao.model.StatusPlanoAlimentar;
import com.nuvexa.relatorios.RelatorioColuna;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.support.MessageSourceAccessor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoAlimentarRelatorioLinhaDTO {

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Long id;
    private String pacienteNome;
    private String nome;
    private LocalDate dataInicio;
    private Integer calorias;
    private Integer refeicoesPorDia;
    private StatusPlanoAlimentar status;
    private String autorNome;

    public static PlanoAlimentarRelatorioLinhaDTO from(PlanoAlimentar planoAlimentar) {
        return PlanoAlimentarRelatorioLinhaDTO.builder()
                .id(planoAlimentar.getId())
                .pacienteNome(planoAlimentar.getPaciente().getNome())
                .nome(planoAlimentar.getNome())
                .dataInicio(planoAlimentar.getDataInicio())
                .calorias(planoAlimentar.getCalorias())
                .refeicoesPorDia(planoAlimentar.getRefeicoesPorDia())
                .status(planoAlimentar.getStatus())
                .autorNome(planoAlimentar.getAutor().getNome())
                .build();
    }

    public static List<RelatorioColuna<PlanoAlimentarRelatorioLinhaDTO>> colunas(MessageSourceAccessor mensagens) {
        return List.of(
                coluna("paciente", "relatorio.planoAlimentar.coluna.paciente", 1, mensagens,
                        PlanoAlimentarRelatorioLinhaDTO::getPacienteNome),
                coluna("plano", "relatorio.planoAlimentar.coluna.plano", 2, mensagens,
                        PlanoAlimentarRelatorioLinhaDTO::getNome),
                coluna("dataInicio", "relatorio.planoAlimentar.coluna.dataInicio", 3, mensagens,
                        row -> row.getDataInicio().format(DATA_FORMATTER)),
                coluna("calorias", "relatorio.planoAlimentar.coluna.calorias", 4, mensagens,
                        PlanoAlimentarRelatorioLinhaDTO::getCalorias),
                coluna("refeicoes", "relatorio.planoAlimentar.coluna.refeicoes", 5, mensagens,
                        PlanoAlimentarRelatorioLinhaDTO::getRefeicoesPorDia),
                coluna("status", "relatorio.planoAlimentar.coluna.status", 6, mensagens,
                        row -> row.getStatus().getRotulo()),
                coluna("autor", "relatorio.planoAlimentar.coluna.autor", 7, mensagens,
                        PlanoAlimentarRelatorioLinhaDTO::getAutorNome));
    }

    private static RelatorioColuna<PlanoAlimentarRelatorioLinhaDTO> coluna(
            String chave, String chaveMensagem, int ordem, MessageSourceAccessor mensagens,
            Function<PlanoAlimentarRelatorioLinhaDTO, Object> extratorValor) {
        return RelatorioColuna.of(chave, mensagens.getMessage(chaveMensagem), ordem, extratorValor);
    }
}
