package com.nuvexa.core.dto.request;

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
import org.modelmapper.ModelMapper;

/**
 * Sem {@code pacienteId} de propósito, mesma regra de {@code ConsultaUpdateRequestDTO}: a quem o
 * prontuário pertence não é editável — só criável. {@code autorId} é resolvido e setado à parte
 * pelo service, sem passar pelo {@code ModelMapper}, por ser id de relação, não campo escalar.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProntuarioUpdateRequestDTO {

    @NotNull(message = "{prontuario.autorId.obrigatorio}")
    private Long autorId;

    @NotNull(message = "{prontuario.secao.obrigatoria}")
    private SecaoProntuario secao;

    @NotNull(message = "{prontuario.status.obrigatorio}")
    private StatusProntuario status;

    private String conteudo;

    private boolean comAnexo;

    public void atualizar(Prontuario prontuario, Usuario autor, ModelMapper modelMapper) {
        modelMapper.map(this, prontuario);
        prontuario.setAutor(autor);
    }
}
