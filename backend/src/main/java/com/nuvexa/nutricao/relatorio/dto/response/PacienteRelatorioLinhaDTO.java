package com.nuvexa.nutricao.relatorio.dto.response;

import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.relatorios.RelatorioColuna;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.nutricao.model.PerfilNutricional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.support.MessageSourceAccessor;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteRelatorioLinhaDTO {

    private Long id;
    private String nome;
    private Integer idade;
    private Sexo sexo;
    private Objetivo objetivo;
    private NivelAtividade nivelAtividade;
    private BigDecimal imc;
    private String classificacaoImc;
    private BigDecimal gastoCaloricoDiario;

    public static PacienteRelatorioLinhaDTO from(
            PerfilNutricional perfilNutricional, int idade, BigDecimal imc, String classificacaoImc, BigDecimal gastoCaloricoDiario) {
        Paciente paciente = perfilNutricional.getPaciente();
        return PacienteRelatorioLinhaDTO.builder()
                .id(paciente.getId())
                .nome(paciente.getNome())
                .idade(idade)
                .sexo(paciente.getSexo())
                .objetivo(perfilNutricional.getObjetivo())
                .nivelAtividade(perfilNutricional.getNivelAtividade())
                .imc(imc)
                .classificacaoImc(classificacaoImc)
                .gastoCaloricoDiario(gastoCaloricoDiario)
                .build();
    }

    public static List<RelatorioColuna<PacienteRelatorioLinhaDTO>> colunas(MessageSourceAccessor mensagens) {
        return List.of(
                coluna("nome", "relatorio.paciente.coluna.nome", 1, mensagens, PacienteRelatorioLinhaDTO::getNome),
                coluna("idade", "relatorio.paciente.coluna.idade", 2, mensagens, PacienteRelatorioLinhaDTO::getIdade),
                coluna("sexo", "relatorio.paciente.coluna.sexo", 3, mensagens,
                        row -> row.getSexo().getRotulo()),
                coluna("objetivo", "relatorio.paciente.coluna.objetivo", 4, mensagens,
                        row -> row.getObjetivo().getRotulo()),
                coluna("nivelAtividade", "relatorio.paciente.coluna.nivelAtividade", 5, mensagens,
                        row -> row.getNivelAtividade().getRotulo()),
                coluna("imc", "relatorio.paciente.coluna.imc", 6, mensagens, PacienteRelatorioLinhaDTO::getImc),
                coluna("classificacaoImc", "relatorio.paciente.coluna.classificacaoImc", 7, mensagens,
                        PacienteRelatorioLinhaDTO::getClassificacaoImc),
                coluna("gastoCaloricoDiario", "relatorio.paciente.coluna.gastoCaloricoDiario", 8, mensagens,
                        PacienteRelatorioLinhaDTO::getGastoCaloricoDiario));
    }

    private static RelatorioColuna<PacienteRelatorioLinhaDTO> coluna(
            String chave, String chaveMensagem, int ordem, MessageSourceAccessor mensagens,
            Function<PacienteRelatorioLinhaDTO, Object> extratorValor) {
        return RelatorioColuna.of(chave, mensagens.getMessage(chaveMensagem), ordem, extratorValor);
    }
}
