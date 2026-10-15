package com.nuvexa.core.controller;

import com.nuvexa.core.dto.request.MembroPapelRequestDTO;
import com.nuvexa.core.dto.response.MembroResponseDTO;
import com.nuvexa.core.service.EquipeService;
import com.nuvexa.platform.web.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Membros da organização atual (vínculos). */
@RestController
@RequestMapping("/api/v1/equipe")
@RequiredArgsConstructor
@PreAuthorize("@permissoes.pode('GERENCIAR_EQUIPE')")
public class EquipeController extends BaseController {

    private final EquipeService equipeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MembroResponseDTO> listar() {
        return equipeService.listar();
    }

    @PutMapping("/{vinculoId}/papel")
    @ResponseStatus(HttpStatus.OK)
    public MembroResponseDTO alterarPapel(@PathVariable Long vinculoId, @Valid @RequestBody MembroPapelRequestDTO request) {
        return equipeService.alterarPapel(vinculoId, request);
    }

    @DeleteMapping("/{vinculoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long vinculoId) {
        equipeService.remover(vinculoId);
    }
}
