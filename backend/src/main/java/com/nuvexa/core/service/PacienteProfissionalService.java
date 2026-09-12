package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.PacienteProfissionalCreateRequestDTO;
import com.nuvexa.core.dto.response.PacienteProfissionalResponseDTO;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.PacienteProfissional;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.PacienteProfissionalRepository;
import com.nuvexa.platform.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class PacienteProfissionalService {

    private final PacienteProfissionalRepository pacienteProfissionalRepository;
    private final ContextoDeAutenticacao contextoDeAutenticacao;
    private final MessageSourceAccessor mensagens;
    private final ValidadorOrganizacional validadorOrganizacional;

    /**
     * Vincula um profissional a um paciente, ambos da organização atual. Se já existir um
     * vínculo inativo para o mesmo par, reativa em vez de criar outra linha — a constraint
     * UNIQUE(paciente_id, profissional_id) não permite duas linhas para o mesmo par de qualquer
     * forma (mesmo padrão de {@link Vinculo}, que também não distingue ativo/inativo na unique).
     */
    public PacienteProfissionalResponseDTO vincular(PacienteProfissionalCreateRequestDTO request) {
        Long organizacaoAtualId = contextoDeAutenticacao.organizacaoAtualId();

        Paciente paciente = buscarPacienteOuFalhar(request.getPacienteId(), organizacaoAtualId);
        Usuario profissional = buscarProfissionalOuFalhar(request.getProfissionalId(), organizacaoAtualId);

        Optional<PacienteProfissional> existente = pacienteProfissionalRepository
                .findByPacienteIdAndProfissionalId(paciente.getId(), profissional.getId());

        if (existente.isPresent()) {
            return reativar(existente.get());
        }

        PacienteProfissional novo = pacienteProfissionalRepository.save(PacienteProfissional.builder()
                .paciente(paciente)
                .profissional(profissional)
                .organizacao(contextoDeAutenticacao.organizacaoAtual())
                .ativo(true)
                .build());
        log.info("Vínculo paciente-profissional criado com id={} (paciente={}, profissional={})",
                novo.getId(), paciente.getId(), profissional.getId());
        return PacienteProfissionalResponseDTO.from(novo);
    }

    private PacienteProfissionalResponseDTO reativar(PacienteProfissional vinculo) {
        if (vinculo.isAtivo()) {
            throw new NegocioException(HttpStatus.CONFLICT, resolveMessage("pacienteProfissional.vinculo.duplicado"));
        }
        vinculo.setAtivo(true);
        PacienteProfissional reativado = pacienteProfissionalRepository.save(vinculo);
        log.info("Vínculo paciente-profissional reativado com id={}", reativado.getId());
        return PacienteProfissionalResponseDTO.from(reativado);
    }

    /**
     * Busca sempre restrita à organização atual — mesma regra de Paciente/Consulta: id de
     * outra organização vira 404, não 403 (um 403 confirmaria que aquele id existe).
     */
    private Paciente buscarPacienteOuFalhar(Long pacienteId, Long organizacaoAtualId) {
        return validadorOrganizacional.pacienteDaOrganizacao(pacienteId, organizacaoAtualId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("paciente.naoEncontrado", pacienteId)));
    }

    /**
     * Profissional válido = Usuario com Vinculo ativo na organização atual. Não usa
     * Usuario.perfil como critério de propósito — decisão explícita para não confundir papel de
     * plataforma (ADMIN/PROFISSIONAL) com papel organizacional (PapelOrganizacional).
     */
    private Usuario buscarProfissionalOuFalhar(Long profissionalId, Long organizacaoAtualId) {
        return validadorOrganizacional.usuarioAtivoNaOrganizacao(profissionalId, organizacaoAtualId)
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST,
                        resolveMessage("pacienteProfissional.profissional.invalido", profissionalId)));
    }

    private String resolveMessage(String key, Object... args) {
        return mensagens.getMessage(key, args);
    }
}
