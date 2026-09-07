package com.nuvexa.nutricao.service;

import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.core.service.OrganizacaoScopedContext;
import com.nuvexa.nutricao.calculator.GastoCaloricoCalculator;
import com.nuvexa.nutricao.calculator.ImcCalculator;
import com.nuvexa.nutricao.calculator.TaxaMetabolicaCalculator;
import com.nuvexa.nutricao.dto.request.PerfilNutricionalCreateRequestDTO;
import com.nuvexa.nutricao.dto.request.PerfilNutricionalUpdateRequestDTO;
import com.nuvexa.nutricao.dto.response.PerfilNutricionalEnumsResponseDTO;
import com.nuvexa.nutricao.dto.response.PerfilNutricionalResponseDTO;
import com.nuvexa.nutricao.model.PerfilNutricional;
import com.nuvexa.nutricao.model.QPerfilNutricional;
import com.nuvexa.nutricao.repository.PerfilNutricionalRepository;
import com.nuvexa.core.model.QPaciente;
import com.nuvexa.platform.exception.NegocioException;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Dado clínico de nutrição (altura/peso/objetivo/nível de atividade/IMC/TMB/gasto calórico) do
 * paciente — referencia {@link Paciente} (core) por FK 1:1, sem duplicar nenhum campo genérico.
 * Paciente em si (nome/data de nascimento/sexo) é responsabilidade de
 * {@code com.nuvexa.core.service.PacienteService}.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class PerfilNutricionalService {

    private final PerfilNutricionalRepository perfilNutricionalRepository;
    private final PacienteRepository pacienteRepository;
    private final ModelMapper modelMapper;
    private final ImcCalculator imcCalculator;
    private final TaxaMetabolicaCalculator taxaMetabolicaCalculator;
    private final GastoCaloricoCalculator gastoCaloricoCalculator;
    private final OrganizacaoScopedContext contexto;

    public PerfilNutricionalResponseDTO create(Long pacienteId, PerfilNutricionalCreateRequestDTO request) {
        Paciente paciente = buscarPacienteOuFalhar(pacienteId);
        validar(request.getAltura(), request.getPeso(), request.getCaloriasDiariasManuais());

        PerfilNutricional perfilNutricional = perfilNutricionalRepository.save(request.toPerfilNutricional(paciente));
        log.info("Perfil nutricional criado para paciente com id={}", pacienteId);
        return toResponseComCalculos(perfilNutricional);
    }

    public PerfilNutricionalResponseDTO update(Long pacienteId, PerfilNutricionalUpdateRequestDTO request) {
        PerfilNutricional perfilNutricional = buscarPerfilOuFalhar(pacienteId);
        validar(request.getAltura(), request.getPeso(), request.getCaloriasDiariasManuais());

        request.atualizar(perfilNutricional, modelMapper);
        PerfilNutricional saved = perfilNutricionalRepository.save(perfilNutricional);
        log.info("Perfil nutricional atualizado para paciente com id={}", pacienteId);
        return toResponseComCalculos(saved);
    }

    public PerfilNutricionalResponseDTO findByPacienteId(Long pacienteId) {
        return toResponseComCalculos(buscarPerfilOuFalhar(pacienteId));
    }

    /**
     * Alimenta a listagem de Pacientes (que mostra Objetivo/IMC por linha) — só retorna quem tem
     * perfil nutricional cadastrado, mesmo comportamento de antes da separação Paciente/core.
     */
    public List<PerfilNutricionalResponseDTO> findAll(String busca) {
        return buscarPerfis(normalizarBusca(busca)).stream()
                .map(this::toResponseComCalculos)
                .toList();
    }

    private List<PerfilNutricional> buscarPerfis(String busca) {
        QPerfilNutricional perfilNutricional = QPerfilNutricional.perfilNutricional;
        QPaciente paciente = QPaciente.paciente;

        BooleanExpression filtroOrganizacao = paciente.organizacao.id.eq(contexto.getContextoDeAutenticacao().organizacaoAtualId());
        BooleanExpression filtroBusca = busca == null ? null : paciente.nome.containsIgnoreCase(busca);

        return contexto.getQueryFactory()
                .selectFrom(perfilNutricional)
                .join(perfilNutricional.paciente, paciente).fetchJoin()
                .where(filtroOrganizacao, filtroBusca)
                .orderBy(paciente.nome.asc())
                .fetch();
    }

    private String normalizarBusca(String busca) {
        return (busca == null || busca.isBlank()) ? null : busca.trim();
    }

    public PerfilNutricionalEnumsResponseDTO getEnums() {
        return PerfilNutricionalEnumsResponseDTO.of();
    }

    /** Remove o perfil nutricional — o Paciente em si é preservado (histórico de Consulta/Prontuário/etc. não depende do perfil). */
    public void delete(Long pacienteId) {
        PerfilNutricional perfilNutricional = buscarPerfilOuFalhar(pacienteId);
        perfilNutricionalRepository.delete(perfilNutricional);
        log.info("Perfil nutricional removido para paciente com id={} (Paciente preservado)", pacienteId);
    }

    private void validar(BigDecimal altura, BigDecimal peso, BigDecimal caloriasDiariasManuais) {
        if (altura == null || altura.signum() <= 0) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("paciente.altura.invalida"));
        }
        if (peso == null || peso.signum() <= 0) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("paciente.peso.invalido"));
        }
        if (caloriasDiariasManuais != null && caloriasDiariasManuais.signum() <= 0) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("paciente.caloriasDiariasManuais.invalida"));
        }
    }

    private Paciente buscarPacienteOuFalhar(Long pacienteId) {
        return pacienteRepository
                .findByIdAndOrganizacaoId(pacienteId, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("paciente.naoEncontrado", pacienteId)));
    }

    /**
     * Busca sempre restrita à organização atual, mesma regra de Paciente/Consulta: id de outra
     * organização vira 404, não 403.
     */
    private PerfilNutricional buscarPerfilOuFalhar(Long pacienteId) {
        return perfilNutricionalRepository
                .findByPacienteIdAndPacienteOrganizacaoId(pacienteId, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("perfilNutricional.naoEncontrado", pacienteId)));
    }

    private String resolveMessage(String key, Object... args) {
        return contexto.getMensagens().getMessage(key, args);
    }

    private PerfilNutricionalResponseDTO toResponseComCalculos(PerfilNutricional perfilNutricional) {
        Paciente paciente = perfilNutricional.getPaciente();

        int idade = Period.between(paciente.getDataNascimento(), LocalDate.now()).getYears();
        BigDecimal imc = imcCalculator.calculate(perfilNutricional.getPeso(), perfilNutricional.getAltura());
        BigDecimal taxaMetabolicaBasal = taxaMetabolicaCalculator.calculate(perfilNutricional.getPeso(), perfilNutricional.getAltura(), idade, paciente.getSexo());
        BigDecimal gastoCaloricoDiario = perfilNutricional.getCaloriasDiariasManuais() != null
                ? perfilNutricional.getCaloriasDiariasManuais()
                : gastoCaloricoCalculator.calculate(taxaMetabolicaBasal, perfilNutricional.getNivelAtividade());

        return PerfilNutricionalResponseDTO.from(perfilNutricional, imc, imcCalculator.classify(imc), taxaMetabolicaBasal, gastoCaloricoDiario);
    }
}
