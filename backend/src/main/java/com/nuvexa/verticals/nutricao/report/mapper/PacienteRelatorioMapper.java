package com.nuvexa.verticals.nutricao.report.mapper;

import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.core.paciente.model.Paciente;
import com.nuvexa.platform.dto.EnumOpcaoDTO;
import com.nuvexa.platform.report.RelatorioColuna;
import com.nuvexa.platform.util.EnumOpcaoResolver;
import com.nuvexa.verticals.nutricao.model.NivelAtividade;
import com.nuvexa.verticals.nutricao.model.Objetivo;
import com.nuvexa.verticals.nutricao.model.PerfilNutricional;
import com.nuvexa.verticals.nutricao.report.dto.response.PacienteRelatorioLinhaDTO;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PacienteRelatorioMapper {

    public List<RelatorioColuna<PacienteRelatorioLinhaDTO>> buildColunas(MessageSource messageSource, Locale locale) {
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

    public PacienteRelatorioLinhaDTO toRow(
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

    private Map<String, String> mapaRotulos(List<EnumOpcaoDTO> opcoes) {
        return opcoes.stream().collect(Collectors.toMap(EnumOpcaoDTO::getValor, EnumOpcaoDTO::getRotulo));
    }

    private RelatorioColuna<PacienteRelatorioLinhaDTO> coluna(
            String chave, String chaveMensagem, int ordem, MessageSource messageSource, Locale locale,
            Function<PacienteRelatorioLinhaDTO, Object> extratorValor) {
        return RelatorioColuna.of(chave, messageSource.getMessage(chaveMensagem, null, locale), ordem, extratorValor);
    }
}
