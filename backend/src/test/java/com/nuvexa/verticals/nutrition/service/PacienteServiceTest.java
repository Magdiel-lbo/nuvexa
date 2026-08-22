package com.nuvexa.verticais.nutricao.service;

import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.core.paciente.model.Paciente;
import com.nuvexa.core.paciente.repository.PacienteRepository;
import com.nuvexa.plataforma.excecao.NegocioException;
import com.nuvexa.verticais.nutricao.calculadora.ImcCalculator;
import com.nuvexa.verticais.nutricao.calculadora.GastoCaloricoCalculator;
import com.nuvexa.verticais.nutricao.calculadora.TaxaMetabolicaCalculator;
import com.nuvexa.verticais.nutricao.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.verticais.nutricao.dto.request.PacienteUpdateRequestDTO;
import com.nuvexa.verticais.nutricao.dto.response.PacienteResponseDTO;
import com.nuvexa.verticais.nutricao.mapper.PacienteMapper;
import com.nuvexa.verticais.nutricao.model.NivelAtividade;
import com.nuvexa.verticais.nutricao.model.Objetivo;
import com.nuvexa.verticais.nutricao.model.PerfilNutricional;
import com.nuvexa.verticais.nutricao.repository.PerfilNutricionalRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PerfilNutricionalRepository perfilNutricionalRepository;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private MessageSource messageSource;

    private PacienteService pacienteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(messageSource.getMessage(any(String.class), any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> {
            Paciente paciente = invocation.getArgument(0);
            if (paciente.getId() == null) {
                paciente.setId(1L);
            }
            return paciente;
        });
        when(perfilNutricionalRepository.save(any(PerfilNutricional.class))).thenAnswer(invocation -> {
            PerfilNutricional profile = invocation.getArgument(0);
            if (profile.getId() == null) {
                profile.setId(100L);
            }
            return profile;
        });
        pacienteService = new PacienteService(
                pacienteRepository,
                perfilNutricionalRepository,
                new PacienteMapper(),
                queryFactory,
                messageSource,
                new ImcCalculator(messageSource),
                new TaxaMetabolicaCalculator(),
                new GastoCaloricoCalculator());
    }

    private PacienteCreateRequestDTO validCreateRequest() {
        PacienteCreateRequestDTO request = new PacienteCreateRequestDTO();
        request.setName("Maria Souza");
        request.setBirthDate(LocalDate.of(1990, 5, 20));
        request.setGender(Sexo.FEMININO);
        request.setHeight(new BigDecimal("1.65"));
        request.setWeight(new BigDecimal("62.50"));
        request.setGoal(Objetivo.EMAGRECIMENTO);
        request.setActivityLevel(NivelAtividade.MODERADAMENTE_ATIVO);
        return request;
    }

    @Test
    void shouldCreatePatientWhenDataIsValid() {
        PacienteResponseDTO response = pacienteService.create(validCreateRequest());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Maria Souza");
    }

    static Stream<Arguments> invalidCreateRequests() {
        return Stream.of(
                Arguments.of("blank name", (java.util.function.Consumer<PacienteCreateRequestDTO>) r -> r.setName("  ")),
                Arguments.of("null name", (java.util.function.Consumer<PacienteCreateRequestDTO>) r -> r.setName(null)),
                Arguments.of("future birthDate", (java.util.function.Consumer<PacienteCreateRequestDTO>) r -> r.setBirthDate(LocalDate.now().plusDays(1))),
                Arguments.of("zero height", (java.util.function.Consumer<PacienteCreateRequestDTO>) r -> r.setHeight(BigDecimal.ZERO)),
                Arguments.of("negative height", (java.util.function.Consumer<PacienteCreateRequestDTO>) r -> r.setHeight(new BigDecimal("-1.70"))),
                Arguments.of("zero weight", (java.util.function.Consumer<PacienteCreateRequestDTO>) r -> r.setWeight(BigDecimal.ZERO)),
                Arguments.of("negative weight", (java.util.function.Consumer<PacienteCreateRequestDTO>) r -> r.setWeight(new BigDecimal("-60"))),
                Arguments.of("zero manualDailyCalories", (java.util.function.Consumer<PacienteCreateRequestDTO>) r -> r.setManualDailyCalories(BigDecimal.ZERO)),
                Arguments.of("negative manualDailyCalories", (java.util.function.Consumer<PacienteCreateRequestDTO>) r -> r.setManualDailyCalories(new BigDecimal("-500")))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateRequests")
    void shouldRejectInvalidPatientData(String scenario, java.util.function.Consumer<PacienteCreateRequestDTO> mutator) {
        PacienteCreateRequestDTO request = validCreateRequest();
        mutator.accept(request);

        assertThatThrownBy(() -> pacienteService.create(request))
                .isInstanceOf(NegocioException.class);

        verify(pacienteRepository, never()).save(any());
        verify(perfilNutricionalRepository, never()).save(any());
    }

    @Test
    void shouldPopulateCalculatedFieldsOnCreate() {
        PacienteCreateRequestDTO request = validCreateRequest();
        request.setBirthDate(LocalDate.now().minusYears(30));
        request.setWeight(new BigDecimal("80"));
        request.setHeight(new BigDecimal("1.80"));
        request.setGender(Sexo.MASCULINO);
        request.setActivityLevel(NivelAtividade.SEDENTARIO);

        PacienteResponseDTO response = pacienteService.create(request);

        assertThat(response.getAge()).isEqualTo(30);
        assertThat(response.getBmi()).isEqualByComparingTo("24.69");
        assertThat(response.getBmiClassification()).isEqualTo("imc.classificacao.normal");
        // Mifflin-St Jeor: 10*80 + 6.25*180 - 5*30 + 5 = 1780
        assertThat(response.getBmr()).isEqualByComparingTo("1780.00");
        // TDEE = BMR * fator sedentário (1.2)
        assertThat(response.getDailyCalorieExpenditure()).isEqualByComparingTo("2136.00");
    }

    @Test
    void shouldUseManualDailyCaloriesInsteadOfCalculatedWhenProvided() {
        PacienteCreateRequestDTO request = validCreateRequest();
        request.setManualDailyCalories(new BigDecimal("3000"));

        PacienteResponseDTO response = pacienteService.create(request);

        assertThat(response.getDailyCalorieExpenditure()).isEqualByComparingTo("3000");
    }

    private PerfilNutricional existingProfile(Long patientId, String name, Sexo gender, Objetivo goal, NivelAtividade activityLevel) {
        Paciente paciente = Paciente.builder()
                .id(patientId)
                .nome(name)
                .dataNascimento(LocalDate.of(1985, 1, 1))
                .sexo(gender)
                .build();
        return PerfilNutricional.builder()
                .id(50L)
                .paciente(paciente)
                .altura(new BigDecimal("1.80"))
                .peso(new BigDecimal("90.00"))
                .objetivo(goal)
                .nivelAtividade(activityLevel)
                .build();
    }

    @Test
    void shouldUpdateExistingPatient() {
        PerfilNutricional existing = existingProfile(1L, "Old Name", Sexo.MASCULINO, Objetivo.MANUTENCAO_PESO, NivelAtividade.SEDENTARIO);
        when(perfilNutricionalRepository.findByPacienteId(1L)).thenReturn(Optional.of(existing));

        PacienteUpdateRequestDTO request = new PacienteUpdateRequestDTO();
        request.setName("New Name");
        request.setBirthDate(LocalDate.of(1985, 1, 1));
        request.setGender(Sexo.MASCULINO);
        request.setHeight(new BigDecimal("1.80"));
        request.setWeight(new BigDecimal("88.00"));
        request.setGoal(Objetivo.EMAGRECIMENTO);
        request.setActivityLevel(NivelAtividade.LEVEMENTE_ATIVO);

        PacienteResponseDTO response = pacienteService.update(1L, request);

        assertThat(response.getName()).isEqualTo("New Name");
        assertThat(response.getWeight()).isEqualByComparingTo("88.00");
        assertThat(response.getGoal()).isEqualTo(Objetivo.EMAGRECIMENTO);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentPatient() {
        when(perfilNutricionalRepository.findByPacienteId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.update(99L, validUpdateRequest()))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void shouldThrowWhenFindingNonExistentPatient() {
        when(perfilNutricionalRepository.findByPacienteId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.findById(99L))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentPatient() {
        when(perfilNutricionalRepository.findByPacienteId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.delete(99L))
                .isInstanceOf(NegocioException.class);

        verify(perfilNutricionalRepository, never()).delete(any());
    }

    @Test
    void shouldDeleteOnlyNutritionProfileAndPreservePatientCore() {
        PerfilNutricional existing = existingProfile(1L, "Maria Souza", Sexo.FEMININO, Objetivo.MANUTENCAO_PESO, NivelAtividade.SEDENTARIO);
        when(perfilNutricionalRepository.findByPacienteId(1L)).thenReturn(Optional.of(existing));

        pacienteService.delete(1L);

        verify(perfilNutricionalRepository).delete(existing);
        verify(pacienteRepository, never()).delete(any());
        verify(pacienteRepository, never()).deleteById(any());
    }

    private PacienteUpdateRequestDTO validUpdateRequest() {
        PacienteUpdateRequestDTO request = new PacienteUpdateRequestDTO();
        request.setName("Someone");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setGender(Sexo.MASCULINO);
        request.setHeight(new BigDecimal("1.75"));
        request.setWeight(new BigDecimal("75.00"));
        request.setGoal(Objetivo.MANUTENCAO_PESO);
        request.setActivityLevel(NivelAtividade.SEDENTARIO);
        return request;
    }
}
