package com.nuvexa.nutricao.report.dto.response;

import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.platform.dto.EnumOpcaoDTO;
import com.nuvexa.platform.util.EnumOpcaoResolver;
import com.nuvexa.relatorios.RelatorioColuna;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.nutricao.model.PerfilNutricional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public static List<RelatorioColuna<PacienteRelatorioLinhaDTO>> colunas(MessageSource messageSource, Locale locale) {
        Map<String, String> rotulosSexo = mapaRotulos(EnumOpcaoResolver.resolve(Sexo.class, "enum.sexo", messageSource, locale));
        Map<String, String> rotulosObjetivo = mapaRotulos(EnumOpcaoResolver.resolve(Objetivo.class, "enum.objetivo", messageSource, locale));
        Map<String, String> rotulosNivelAtividade = mapaRotulos(
                EnumOpcaoResolver.resolve(NivelAtividade.class, "enum.nivelAtividade", messageSource, locale));

        return List.of(
                coluna("nome", "relatorio.paciente.coluna.nome", 1, messageSource, locale, PacienteRelatorioLinhaDTO::getNome),
                coluna("idade", "relatorio.paciente.coluna.idade", 2, messageSource, locale, PacienteRelatorioLinhaDTO::getIdade),
                coluna("sexo", "relatorio.paciente.coluna.sexo", 3, messageSource, locale,
                        row -> rotulosSexo.get(row.getSexo().name())),
                coluna("objetivo", "relatorio.paciente.coluna.objetivo", 4, messageSource, locale,
                        row -> rotulosObjetivo.get(row.getObjetivo().name())),
                coluna("nivelAtividade", "relatorio.paciente.coluna.nivelAtividade", 5, messageSource, locale,
                        row -> rotulosNivelAtividade.get(row.getNivelAtividade().name())),
                coluna("imc", "relatorio.paciente.coluna.imc", 6, messageSource, locale, PacienteRelatorioLinhaDTO::getImc),
                coluna("classificacaoImc", "relatorio.paciente.coluna.classificacaoImc", 7, messageSource, locale,
                        PacienteRelatorioLinhaDTO::getClassificacaoImc),
                coluna("gastoCaloricoDiario", "relatorio.paciente.coluna.gastoCaloricoDiario", 8, messageSource, locale,
                        PacienteRelatorioLinhaDTO::getGastoCaloricoDiario));
    }

    private static Map<String, String> mapaRotulos(List<EnumOpcaoDTO> opcoes) {
        return opcoes.stream().collect(Collectors.toMap(EnumOpcaoDTO::getValor, EnumOpcaoDTO::getRotulo));
    }

    private static RelatorioColuna<PacienteRelatorioLinhaDTO> coluna(
            String chave, String chaveMensagem, int ordem, MessageSource messageSource, Locale locale,
            Function<PacienteRelatorioLinhaDTO, Object> extratorValor) {
        return RelatorioColuna.of(chave, messageSource.getMessage(chaveMensagem, null, locale), ordem, extratorValor);
    }
}
