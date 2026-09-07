package com.nuvexa.nutricao.dto.request;

import com.nuvexa.core.model.Usuario;
import com.nuvexa.nutricao.model.Avaliacao;
import com.nuvexa.nutricao.model.StatusAvaliacao;
import com.nuvexa.nutricao.model.TipoAvaliacao;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sem {@code pacienteId} de propósito, mesma regra de {@code ProntuarioUpdateRequestDTO}: a quem
 * a avaliação pertence não é editável — só criável. {@code avaliadorId} é resolvido e setado à
 * parte pelo service, sem passar pelo ModelMapper, por ser id de relação, não campo escalar.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoUpdateRequestDTO {

    @NotNull(message = "{avaliacao.avaliadorId.obrigatorio}")
    private Long avaliadorId;

    @NotNull(message = "{avaliacao.data.obrigatoria}")
    private LocalDate data;

    @NotNull(message = "{avaliacao.tipo.obrigatorio}")
    private TipoAvaliacao tipo;

    @NotNull(message = "{avaliacao.status.obrigatorio}")
    private StatusAvaliacao status;

    private BigDecimal peso;

    private BigDecimal percentualGordura;

    public void atualizar(Avaliacao avaliacao, Usuario avaliador, ModelMapper modelMapper) {
        modelMapper.map(this, avaliacao);
        avaliacao.setAvaliador(avaliador);
    }
}
