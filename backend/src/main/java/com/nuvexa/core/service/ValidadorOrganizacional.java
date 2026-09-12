package com.nuvexa.core.service;

import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.core.repository.VinculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ValidadorOrganizacional {

    private final PacienteRepository pacienteRepository;
    private final VinculoRepository vinculoRepository;

    public Optional<Paciente> pacienteDaOrganizacao(Long pacienteId, Long organizacaoId) {
        return pacienteRepository.findByIdAndOrganizacaoId(pacienteId, organizacaoId);
    }

    public Optional<Usuario> usuarioAtivoNaOrganizacao(Long usuarioId, Long organizacaoId) {
        return vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(usuarioId).stream()
                .filter(vinculo -> vinculo.getOrganizacao().getId().equals(organizacaoId))
                .map(Vinculo::getUsuario)
                .findFirst();
    }
}
