package com.nuvexa.core.dto.request;

import com.nuvexa.core.model.StatusConsulta;
import com.nuvexa.core.model.TipoConsulta;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cobre o gap encontrado na auditoria: {@code pacienteId} não tinha {@code @NotNull}, então
 * omiti-lo só era barrado pelo NOT NULL do banco (404 via Service) em vez de 400 de Bean
 * Validation. Representa os 4 DTOs de criação com o mesmo gap (Consulta/Prontuario/Avaliacao/
 * PlanoAlimentar) — validação idêntica nos 4, não precisa duplicar o teste.
 */
class ConsultaCreateRequestDTOTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    private ConsultaCreateRequestDTO requestValido() {
        return ConsultaCreateRequestDTO.builder()
                .pacienteId(1L)
                .profissionalId(2L)
                .dataHora(LocalDateTime.now().plusDays(1))
                .duracaoMinutos(30)
                .tipo(TipoConsulta.PRIMEIRA_CONSULTA)
                .status(StatusConsulta.AGENDADA)
                .build();
    }

    @Test
    void deveFalharValidacaoQuandoPacienteIdAusente() {
        ConsultaCreateRequestDTO request = requestValido();
        request.setPacienteId(null);

        Set<ConstraintViolation<ConsultaCreateRequestDTO>> violacoes = validator.validate(request);

        assertThat(violacoes)
                .anySatisfy(violacao -> assertThat(violacao.getPropertyPath().toString()).isEqualTo("pacienteId"));
    }

    @Test
    void devePassarValidacaoQuandoTodosOsCamposObrigatoriosPreenchidos() {
        Set<ConstraintViolation<ConsultaCreateRequestDTO>> violacoes = validator.validate(requestValido());

        assertThat(violacoes).isEmpty();
    }
}
