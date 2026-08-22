package com.nuvexa.verticais.nutricao.service;

import com.nuvexa.nucleo.paciente.model.Sexo;
import com.nuvexa.nucleo.paciente.model.Paciente;
import com.nuvexa.nucleo.paciente.repository.PacienteRepository;
import com.nuvexa.plataforma.excecao.NegocioException;
import com.nuvexa.plataforma.util.EnumOpcaoResolver;
import com.nuvexa.verticais.nutricao.calculadora.ImcCalculator;
import com.nuvexa.verticais.nutricao.calculadora.GastoCaloricoCalculator;
import com.nuvexa.verticais.nutricao.calculadora.TaxaMetabolicaCalculator;
import com.nuvexa.verticais.nutricao.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.verticais.nutricao.dto.request.PacienteUpdateRequestDTO;
import com.nuvexa.verticais.nutricao.dto.response.PacienteEnumsResponseDTO;
import com.nuvexa.verticais.nutricao.dto.response.PacienteResponseDTO;
import com.nuvexa.verticais.nutricao.mapper.PacienteMapper;
import com.nuvexa.verticais.nutricao.model.NivelAtividade;
import com.nuvexa.verticais.nutricao.model.Objetivo;
import com.nuvexa.verticais.nutricao.model.PerfilNutricional;
import com.nuvexa.verticais.nutricao.model.QPerfilNutricional;
import com.nuvexa.verticais.nutricao.repository.PerfilNutricionalRepository;
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
                .genders(EnumOpcaoResolver.resolve(Sexo.class, "enum.gender", messageSource, MESSAGE_LOCALE))
                .goals(EnumOpcaoResolver.resolve(Objetivo.class, "enum.goal", messageSource, MESSAGE_LOCALE))
                .activityLevels(EnumOpcaoResolver.resolve(NivelAtividade.class, "enum.activityLevel", messageSource, MESSAGE_LOCALE))
                .build();
    }

    public void delete(Long id) {
        PerfilNutricional perfilNutricional = findProfileOrThrow(id);
        perfilNutricionalRepository.delete(perfilNutricional);
        log.info("Perfil nutricional removido para paciente com id={} (Paciente preservado)", id);
    }

    private List<PerfilNutricional> searchProfiles(String name) {
        QPerfilNutricional perfilNutricional = QPerfilNutricional.perfilNutricional;

        BooleanExpression nameFilter = name == null ? null : perfilNutricional.paciente.name.containsIgnoreCase(name);

        return queryFactory
                .selectFrom(perfilNutricional)
                .where(nameFilter)
                .orderBy(perfilNutricional.paciente.name.asc())
                .fetch();
    }

    private String normalizeSearch(String search) {
        return (search == null || search.isBlank()) ? null : search.trim();
    }

    private PerfilNutricional findProfileOrThrow(Long id) {
        return perfilNutricionalRepository.findByPacienteId(id)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("patient.notFound", id)));
    }

    private void validate(String name, LocalDate birthDate, BigDecimal height, BigDecimal weight, BigDecimal manualDailyCalories) {
        if (name == null || name.isBlank()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("patient.name.required"));
        }
        if (birthDate == null || birthDate.isAfter(LocalDate.now())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("patient.birthDate.future"));
        }
        if (height == null || height.signum() <= 0) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("patient.height.invalid"));
        }
        if (weight == null || weight.signum() <= 0) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("patient.weight.invalid"));
        }
        if (manualDailyCalories != null && manualDailyCalories.signum() <= 0) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("patient.manualDailyCalories.invalid"));
        }
    }

    private String resolveMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, MESSAGE_LOCALE);
    }

    private PacienteResponseDTO toResponseWithCalculations(PerfilNutricional perfilNutricional) {
        PacienteResponseDTO response = pacienteMapper.toResponse(perfilNutricional);
        Paciente paciente = perfilNutricional.getPaciente();

        int age = Period.between(paciente.getBirthDate(), LocalDate.now()).getYears();
        BigDecimal bmi = imcCalculator.calculate(perfilNutricional.getWeight(), perfilNutricional.getHeight());
        BigDecimal bmr = taxaMetabolicaCalculator.calculate(perfilNutricional.getWeight(), perfilNutricional.getHeight(), age, paciente.getGender());
        BigDecimal dailyCalorieExpenditure = perfilNutricional.getManualDailyCalories() != null
                ? perfilNutricional.getManualDailyCalories()
                : gastoCaloricoCalculator.calculate(bmr, perfilNutricional.getActivityLevel());

        response.setAge(age);
        response.setBmi(bmi);
        response.setBmiClassification(imcCalculator.classify(bmi));
        response.setBmr(bmr);
        response.setDailyCalorieExpenditure(dailyCalorieExpenditure);

        return response;
    }
}
