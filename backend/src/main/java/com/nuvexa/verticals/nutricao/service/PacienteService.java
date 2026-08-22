package com.nuvexa.verticals.nutricao.service;

import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.core.paciente.model.Paciente;
import com.nuvexa.core.paciente.repository.PacienteRepository;
import com.nuvexa.platform.exception.NegocioException;
import com.nuvexa.platform.util.EnumOpcaoResolver;
import com.nuvexa.verticals.nutricao.calculator.ImcCalculator;
import com.nuvexa.verticals.nutricao.calculator.GastoCaloricoCalculator;
import com.nuvexa.verticals.nutricao.calculator.TaxaMetabolicaCalculator;
import com.nuvexa.verticals.nutricao.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.verticals.nutricao.dto.request.PacienteUpdateRequestDTO;
import com.nuvexa.verticals.nutricao.dto.response.PacienteEnumsResponseDTO;
import com.nuvexa.verticals.nutricao.dto.response.PacienteResponseDTO;
import com.nuvexa.verticals.nutricao.mapper.PacienteMapper;
import com.nuvexa.verticals.nutricao.model.NivelAtividade;
import com.nuvexa.verticals.nutricao.model.Objetivo;
import com.nuvexa.verticals.nutricao.model.PerfilNutricional;
import com.nuvexa.verticals.nutricao.model.QPerfilNutricional;
import com.nuvexa.verticals.nutricao.repository.PerfilNutricionalRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class PacienteService {

    private static final Locale MESSAGE_LOCALE = Locale.of("pt", "BR");

    private final PacienteRepository pacienteRepository;
    private final PerfilNutricionalRepository perfilNutricionalRepository;
    private final PacienteMapper pacienteMapper;
    private final JPAQueryFactory queryFactory;
    private final MessageSource messageSource;
    private final ImcCalculator imcCalculator;
    private final TaxaMetabolicaCalculator taxaMetabolicaCalculator;
    private final GastoCaloricoCalculator gastoCaloricoCalculator;

    public PacienteResponseDTO create(PacienteCreateRequestDTO request) {
        validate(request.getName(), request.getBirthDate(), request.getHeight(), request.getWeight(), request.getManualDailyCalories());

        Paciente paciente = pacienteRepository.save(pacienteMapper.toPaciente(request));
        PerfilNutricional perfilNutricional = perfilNutricionalRepository.save(pacienteMapper.toPerfilNutricional(request, paciente));
        log.info("Paciente criado com id={}", paciente.getId());
        return toResponseWithCalculations(perfilNutricional);
    }

    public PacienteResponseDTO update(Long id, PacienteUpdateRequestDTO request) {
        PerfilNutricional perfilNutricional = findProfileOrThrow(id);
        validate(request.getName(), request.getBirthDate(), request.getHeight(), request.getWeight(), request.getManualDailyCalories());

        pacienteMapper.updatePaciente(request, perfilNutricional.getPaciente());
        pacienteMapper.updatePerfilNutricional(request, perfilNutricional);
        pacienteRepository.save(perfilNutricional.getPaciente());
        PerfilNutricional saved = perfilNutricionalRepository.save(perfilNutricional);
        log.info("Paciente atualizado com id={}", id);
        return toResponseWithCalculations(saved);
    }

    public PacienteResponseDTO findById(Long id) {
        return toResponseWithCalculations(findProfileOrThrow(id));
    }

    public List<PacienteResponseDTO> findAll(String search) {
        return searchProfiles(normalizeSearch(search)).stream()
                .map(this::toResponseWithCalculations)
                .toList();
    }

    public PacienteEnumsResponseDTO getEnums() {
        return PacienteEnumsResponseDTO.builder()
                .genders(EnumOpcaoResolver.resolve(Sexo.class, "enum.sexo", messageSource, MESSAGE_LOCALE))
                .goals(EnumOpcaoResolver.resolve(Objetivo.class, "enum.objetivo", messageSource, MESSAGE_LOCALE))
                .activityLevels(EnumOpcaoResolver.resolve(NivelAtividade.class, "enum.nivelAtividade", messageSource, MESSAGE_LOCALE))
                .build();
    }

    public void delete(Long id) {
        PerfilNutricional perfilNutricional = findProfileOrThrow(id);
        perfilNutricionalRepository.delete(perfilNutricional);
        log.info("Perfil nutricional removido para paciente com id={} (Paciente preservado)", id);
    }

    private List<PerfilNutricional> searchProfiles(String name) {
        QPerfilNutricional perfilNutricional = QPerfilNutricional.perfilNutricional;

        BooleanExpression nameFilter = name == null ? null : perfilNutricional.paciente.nome.containsIgnoreCase(name);

        return queryFactory
                .selectFrom(perfilNutricional)
                .where(nameFilter)
                .orderBy(perfilNutricional.paciente.nome.asc())
                .fetch();
    }

    private String normalizeSearch(String search) {
        return (search == null || search.isBlank()) ? null : search.trim();
    }

    private PerfilNutricional findProfileOrThrow(Long id) {
        return perfilNutricionalRepository.findByPacienteId(id)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("paciente.naoEncontrado", id)));
    }

    private void validate(String name, LocalDate birthDate, BigDecimal height, BigDecimal weight, BigDecimal manualDailyCalories) {
        if (name == null || name.isBlank()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("paciente.nome.obrigatorio"));
        }
        if (birthDate == null || birthDate.isAfter(LocalDate.now())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("paciente.dataNascimento.futura"));
        }
        if (height == null || height.signum() <= 0) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("paciente.altura.invalida"));
        }
        if (weight == null || weight.signum() <= 0) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("paciente.peso.invalido"));
        }
        if (manualDailyCalories != null && manualDailyCalories.signum() <= 0) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("paciente.caloriasDiariasManuais.invalida"));
        }
    }

    private String resolveMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, MESSAGE_LOCALE);
    }

    private PacienteResponseDTO toResponseWithCalculations(PerfilNutricional perfilNutricional) {
        PacienteResponseDTO response = pacienteMapper.toResponse(perfilNutricional);
        Paciente paciente = perfilNutricional.getPaciente();

        int age = Period.between(paciente.getDataNascimento(), LocalDate.now()).getYears();
        BigDecimal bmi = imcCalculator.calculate(perfilNutricional.getPeso(), perfilNutricional.getAltura());
        BigDecimal bmr = taxaMetabolicaCalculator.calculate(perfilNutricional.getPeso(), perfilNutricional.getAltura(), age, paciente.getSexo());
        BigDecimal dailyCalorieExpenditure = perfilNutricional.getCaloriasDiariasManuais() != null
                ? perfilNutricional.getCaloriasDiariasManuais()
                : gastoCaloricoCalculator.calculate(bmr, perfilNutricional.getNivelAtividade());

        response.setAge(age);
        response.setBmi(bmi);
        response.setBmiClassification(imcCalculator.classify(bmi));
        response.setBmr(bmr);
        response.setDailyCalorieExpenditure(dailyCalorieExpenditure);

        return response;
    }
}
