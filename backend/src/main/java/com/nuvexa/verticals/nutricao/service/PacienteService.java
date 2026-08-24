package com.nuvexa.verticals.nutricao.service;

import com.nuvexa.core.contexto.ContextoDeAutenticacao;
import com.nuvexa.core.paciente.model.Paciente;
import com.nuvexa.core.paciente.model.QPaciente;
import com.nuvexa.core.paciente.repository.PacienteRepository;
import com.nuvexa.platform.exception.NegocioException;
import com.nuvexa.verticals.nutricao.calculator.ImcCalculator;
import com.nuvexa.verticals.nutricao.calculator.GastoCaloricoCalculator;
import com.nuvexa.verticals.nutricao.calculator.TaxaMetabolicaCalculator;
import com.nuvexa.verticals.nutricao.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.verticals.nutricao.dto.request.PacienteUpdateRequestDTO;
import com.nuvexa.verticals.nutricao.dto.response.PacienteEnumsResponseDTO;
import com.nuvexa.verticals.nutricao.dto.response.PacienteResponseDTO;
import com.nuvexa.verticals.nutricao.model.PerfilNutricional;
import com.nuvexa.verticals.nutricao.model.QPerfilNutricional;
import com.nuvexa.verticals.nutricao.repository.PerfilNutricionalRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
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
    private final ContextoDeAutenticacao contextoDeAutenticacao;
    private final JPAQueryFactory queryFactory;
    private final MessageSource messageSource;
    private final ModelMapper modelMapper;
    private final ImcCalculator imcCalculator;
    private final TaxaMetabolicaCalculator taxaMetabolicaCalculator;
    private final GastoCaloricoCalculator gastoCaloricoCalculator;

    public PacienteResponseDTO create(PacienteCreateRequestDTO request) {
        validar(request.getNome(), request.getDataNascimento(), request.getAltura(), request.getPeso(), request.getCaloriasDiariasManuais());

        Paciente paciente = pacienteRepository.save(request.toPaciente(contextoDeAutenticacao.organizacaoAtual()));
        PerfilNutricional perfilNutricional = perfilNutricionalRepository.save(request.toPerfilNutricional(paciente));
        log.info("Paciente criado com id={}", paciente.getId());
        return toResponseComCalculos(perfilNutricional);
    }

    public PacienteResponseDTO update(Long id, PacienteUpdateRequestDTO request) {
        PerfilNutricional perfilNutricional = buscarPerfilOuFalhar(id);
        validar(request.getNome(), request.getDataNascimento(), request.getAltura(), request.getPeso(), request.getCaloriasDiariasManuais());

        request.atualizar(perfilNutricional.getPaciente(), modelMapper);
        request.atualizar(perfilNutricional, modelMapper);
        pacienteRepository.save(perfilNutricional.getPaciente());
        PerfilNutricional saved = perfilNutricionalRepository.save(perfilNutricional);
        log.info("Paciente atualizado com id={}", id);
        return toResponseComCalculos(saved);
    }

    public PacienteResponseDTO findById(Long id) {
        return toResponseComCalculos(buscarPerfilOuFalhar(id));
    }

    public List<PacienteResponseDTO> findAll(String busca) {
        return buscarPerfis(normalizarBusca(busca)).stream()
                .map(this::toResponseComCalculos)
                .toList();
    }

    public PacienteEnumsResponseDTO getEnums() {
        return PacienteEnumsResponseDTO.of(messageSource, MESSAGE_LOCALE);
    }

    public void delete(Long id) {
        PerfilNutricional perfilNutricional = buscarPerfilOuFalhar(id);
        perfilNutricionalRepository.delete(perfilNutricional);
        log.info("Perfil nutricional removido para paciente com id={} (Paciente preservado)", id);
    }

    private List<PerfilNutricional> buscarPerfis(String nome) {
        QPerfilNutricional perfilNutricional = QPerfilNutricional.perfilNutricional;
        QPaciente paciente = QPaciente.paciente;

        BooleanExpression filtroNome = nome == null ? null : paciente.nome.containsIgnoreCase(nome);
        // Escopo aplicado na própria query: nenhum paciente de outra organização chega ao Java.
        BooleanExpression filtroOrganizacao = paciente.organizacao.id.eq(contextoDeAutenticacao.organizacaoAtualId());

        return queryFactory
                .selectFrom(perfilNutricional)
                .join(perfilNutricional.paciente, paciente).fetchJoin()
                .where(filtroOrganizacao, filtroNome)
                .orderBy(paciente.nome.asc())
                .fetch();
    }

    private String normalizarBusca(String busca) {
        return (busca == null || busca.isBlank()) ? null : busca.trim();
    }

    /**
     * Busca sempre restrita à organização atual. Paciente de outra organização resulta em 404
     * (e não 403) de propósito: um 403 confirmaria ao chamador que aquele id existe.
     */
    private PerfilNutricional buscarPerfilOuFalhar(Long id) {
        return perfilNutricionalRepository
                .findByPacienteIdAndPacienteOrganizacaoId(id, contextoDeAutenticacao.organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("paciente.naoEncontrado", id)));
    }

    private void validar(String nome, LocalDate dataNascimento, BigDecimal altura, BigDecimal peso, BigDecimal caloriasDiariasManuais) {
        if (nome == null || nome.isBlank()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("paciente.nome.obrigatorio"));
        }
        if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("paciente.dataNascimento.futura"));
        }
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

    private String resolveMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, MESSAGE_LOCALE);
    }

    private PacienteResponseDTO toResponseComCalculos(PerfilNutricional perfilNutricional) {
        PacienteResponseDTO response = PacienteResponseDTO.from(perfilNutricional);
        Paciente paciente = perfilNutricional.getPaciente();

        int idade = Period.between(paciente.getDataNascimento(), LocalDate.now()).getYears();
        BigDecimal imc = imcCalculator.calculate(perfilNutricional.getPeso(), perfilNutricional.getAltura());
        BigDecimal taxaMetabolicaBasal = taxaMetabolicaCalculator.calculate(perfilNutricional.getPeso(), perfilNutricional.getAltura(), idade, paciente.getSexo());
        BigDecimal gastoCaloricoDiario = perfilNutricional.getCaloriasDiariasManuais() != null
                ? perfilNutricional.getCaloriasDiariasManuais()
                : gastoCaloricoCalculator.calculate(taxaMetabolicaBasal, perfilNutricional.getNivelAtividade());

        response.setIdade(idade);
        response.setImc(imc);
        response.setClassificacaoImc(imcCalculator.classify(imc));
        response.setTaxaMetabolicaBasal(taxaMetabolicaBasal);
        response.setGastoCaloricoDiario(gastoCaloricoDiario);

        return response;
    }
}
