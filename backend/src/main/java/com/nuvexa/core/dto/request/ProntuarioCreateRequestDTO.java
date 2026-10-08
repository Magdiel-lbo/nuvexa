package com.nuvexa.core.dto.request;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.SecaoProntuario;
import com.nuvexa.core.model.StatusProntuario;
import com.nuvexa.core.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProntuarioCreateRequestDTO {

    @NotNull(message = "{prontuario.pacienteId.obrigatorio}")
    private Long pacienteId;

    @NotNull(message = "{prontuario.autorId.obrigatorio}")
    private Long autorId;

    @NotNull(message = "{prontuario.secao.obrigatoria}")
    private SecaoProntuario secao;

    @NotNull(message = "{prontuario.status.obrigatorio}")
    private StatusProntuario status;

    private String conteudo;

    @Schema(description = "Indica se o prontuário faz referência a algum anexo — não é derivado automaticamente dos anexos reais cadastrados em /api/v1/prontuarios/{prontuarioId}/anexos, é um flag independente")
    private boolean comAnexo;

    public Prontuario toProntuario(Organizacao organizacao, Paciente paciente, Usuario autor) {
        return Prontuario.builder()
                .organizacao(organizacao)
                .paciente(paciente)
                .autor(autor)
                .secao(secao)
                .status(status)
                .conteudo(conteudo)
                .comAnexo(comAnexo)
                .build();
    }
}
