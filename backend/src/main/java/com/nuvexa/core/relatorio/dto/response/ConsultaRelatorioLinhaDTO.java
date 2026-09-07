package com.nuvexa.core.relatorio.dto.response;

import com.nuvexa.core.model.Consulta;
import com.nuvexa.core.model.StatusConsulta;
import com.nuvexa.core.model.TipoConsulta;
import com.nuvexa.relatorios.RelatorioColuna;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.support.MessageSourceAccessor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaRelatorioLinhaDTO {

    private static final DateTimeFormatter DATA_HORA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private String pacienteNome;
    private LocalDateTime dataHora;
    private TipoConsulta tipo;
    private StatusConsulta status;
    private String profissionalNome;

    public static ConsultaRelatorioLinhaDTO from(Consulta consulta) {
        return ConsultaRelatorioLinhaDTO.builder()
                .id(consulta.getId())
                .pacienteNome(consulta.getPaciente().getNome())
                .dataHora(consulta.getDataHora())
                .tipo(consulta.getTipo())
                .status(consulta.getStatus())
                .profissionalNome(consulta.getProfissional().getNome())
                .build();
    }

    public static List<RelatorioColuna<ConsultaRelatorioLinhaDTO>> colunas(MessageSourceAccessor mensagens) {
        return List.of(
                coluna("paciente", "relatorio.consulta.coluna.paciente", 1, mensagens, ConsultaRelatorioLinhaDTO::getPacienteNome),
                coluna("dataHora", "relatorio.consulta.coluna.dataHora", 2, mensagens,
                        row -> row.getDataHora().format(DATA_HORA_FORMATTER)),
                coluna("tipo", "relatorio.consulta.coluna.tipo", 3, mensagens,
                        row -> row.getTipo().getRotulo()),
                coluna("status", "relatorio.consulta.coluna.status", 4, mensagens,
                        row -> row.getStatus().getRotulo()),
                coluna("profissional", "relatorio.consulta.coluna.profissional", 5, mensagens,
                        ConsultaRelatorioLinhaDTO::getProfissionalNome));
    }

    private static RelatorioColuna<ConsultaRelatorioLinhaDTO> coluna(
            String chave, String chaveMensagem, int ordem, MessageSourceAccessor mensagens,
            Function<ConsultaRelatorioLinhaDTO, Object> extratorValor) {
        return RelatorioColuna.of(chave, mensagens.getMessage(chaveMensagem), ordem, extratorValor);
    }
}
