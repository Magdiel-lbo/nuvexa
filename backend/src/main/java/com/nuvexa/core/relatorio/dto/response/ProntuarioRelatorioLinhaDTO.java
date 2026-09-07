package com.nuvexa.core.relatorio.dto.response;

import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.SecaoProntuario;
import com.nuvexa.core.model.StatusProntuario;
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
public class ProntuarioRelatorioLinhaDTO {

    private static final DateTimeFormatter DATA_HORA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private String registro;
    private String pacienteNome;
    private SecaoProntuario secao;
    private StatusProntuario status;
    private String autorNome;
    private boolean comAnexo;
    private LocalDateTime atualizadoEm;

    public static ProntuarioRelatorioLinhaDTO from(Prontuario prontuario) {
        return ProntuarioRelatorioLinhaDTO.builder()
                .id(prontuario.getId())
                .registro("PR-" + String.format("%04d", prontuario.getId()))
                .pacienteNome(prontuario.getPaciente().getNome())
                .secao(prontuario.getSecao())
                .status(prontuario.getStatus())
                .autorNome(prontuario.getAutor().getNome())
                .comAnexo(prontuario.isComAnexo())
                .atualizadoEm(prontuario.getAtualizadoEm())
                .build();
    }

    public static List<RelatorioColuna<ProntuarioRelatorioLinhaDTO>> colunas(MessageSourceAccessor mensagens) {
        return List.of(
                coluna("paciente", "relatorio.prontuario.coluna.paciente", 1, mensagens,
                        ProntuarioRelatorioLinhaDTO::getPacienteNome),
                coluna("registro", "relatorio.prontuario.coluna.registro", 2, mensagens,
                        ProntuarioRelatorioLinhaDTO::getRegistro),
                coluna("secao", "relatorio.prontuario.coluna.secao", 3, mensagens,
                        row -> row.getSecao().getRotulo()),
                coluna("autor", "relatorio.prontuario.coluna.autor", 4, mensagens,
                        ProntuarioRelatorioLinhaDTO::getAutorNome),
                coluna("status", "relatorio.prontuario.coluna.status", 5, mensagens,
                        row -> row.getStatus().getRotulo()),
                coluna("anexo", "relatorio.prontuario.coluna.anexo", 6, mensagens,
                        row -> mensagens.getMessage(
                                row.isComAnexo() ? "relatorio.prontuario.anexo.sim" : "relatorio.prontuario.anexo.nao")),
                coluna("atualizadoEm", "relatorio.prontuario.coluna.atualizadoEm", 7, mensagens,
                        row -> row.getAtualizadoEm().format(DATA_HORA_FORMATTER)));
    }

    private static RelatorioColuna<ProntuarioRelatorioLinhaDTO> coluna(
            String chave, String chaveMensagem, int ordem, MessageSourceAccessor mensagens,
            Function<ProntuarioRelatorioLinhaDTO, Object> extratorValor) {
        return RelatorioColuna.of(chave, mensagens.getMessage(chaveMensagem), ordem, extratorValor);
    }
}
