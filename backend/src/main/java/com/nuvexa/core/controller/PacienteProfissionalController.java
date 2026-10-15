package com.nuvexa.core.controller;

import com.nuvexa.core.dto.request.PacienteProfissionalCreateRequestDTO;
import com.nuvexa.core.dto.request.ProfissionalVinculoRequestDTO;
import com.nuvexa.core.dto.response.PacienteProfissionalResponseDTO;
import com.nuvexa.core.service.PacienteProfissionalService;
import com.nuvexa.platform.web.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Profissionais responsáveis por um paciente. Registro de responsabilidade — não restringe acesso. */
@RestController
@RequestMapping("/api/v1/pacientes/{pacienteId}/profissionais")
@RequiredArgsConstructor
@PreAuthorize("@permissoes.pode('CADASTRO_AGENDA')")
public class PacienteProfissionalController extends BaseController {

    private final PacienteProfissionalService pacienteProfissionalService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PacienteProfissionalResponseDTO> listar(@PathVariable Long pacienteId) {
        return pacienteProfissionalService.listarDoPaciente(pacienteId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteProfissionalResponseDTO vincular(@PathVariable Long pacienteId, @Valid @RequestBody ProfissionalVinculoRequestDTO request) {
        return pacienteProfissionalService.vincular(PacienteProfissionalCreateRequestDTO.builder()
                .pacienteId(pacienteId)
                .profissionalId(request.getProfissionalId())
                .build());
    }

    @DeleteMapping("/{profissionalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desvincular(@PathVariable Long pacienteId, @PathVariable Long profissionalId) {
        pacienteProfissionalService.desvincular(pacienteId, profissionalId);
    }
}
