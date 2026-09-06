package com.nuvexa.core.dto.request;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.SecaoProntuario;
import com.nuvexa.core.model.StatusProntuario;
import com.nuvexa.core.model.Usuario;
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

    private Long pacienteId;

    @NotNull(message = "{prontuario.autorId.obrigatorio}")
    private Long autorId;

    @NotNull(message = "{prontuario.secao.obrigatoria}")
    private SecaoProntuario secao;

    @NotNull(message = "{prontuario.status.obrigatorio}")
    private StatusProntuario status;

    private String conteudo;

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
