package com.nuvexa.nutricao.dto.request;

import com.nuvexa.core.model.Usuario;
import com.nuvexa.nutricao.model.PlanoAlimentar;
import com.nuvexa.nutricao.model.StatusPlanoAlimentar;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;

/**
 * Sem {@code pacienteId} de propósito, mesma regra de {@code ProntuarioUpdateRequestDTO}: a quem
 * o plano pertence não é editável — só criável. {@code autorId} é resolvido e setado à parte pelo
 * service, sem passar pelo {@code ModelMapper}, por ser id de relação, não campo escalar.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoAlimentarUpdateRequestDTO {

    @NotNull(message = "{planoAlimentar.autorId.obrigatorio}")
    private Long autorId;

    @NotBlank(message = "{planoAlimentar.nome.obrigatorio}")
    private String nome;

    @NotNull(message = "{planoAlimentar.dataInicio.obrigatoria}")
    private LocalDate dataInicio;

    @NotNull(message = "{planoAlimentar.calorias.obrigatorias}")
    @Positive(message = "{planoAlimentar.calorias.invalidas}")
    private Integer calorias;

    @NotNull(message = "{planoAlimentar.refeicoesPorDia.obrigatorio}")
    @Positive(message = "{planoAlimentar.refeicoesPorDia.invalido}")
    private Integer refeicoesPorDia;

    @NotNull(message = "{planoAlimentar.status.obrigatorio}")
    private StatusPlanoAlimentar status;

    public void atualizar(PlanoAlimentar planoAlimentar, Usuario autor, ModelMapper modelMapper) {
        modelMapper.map(this, planoAlimentar);
        planoAlimentar.setAutor(autor);
    }
}
