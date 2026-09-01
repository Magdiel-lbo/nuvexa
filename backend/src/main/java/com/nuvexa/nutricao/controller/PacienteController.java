package com.nuvexa.nutricao.controller;

import com.nuvexa.nutricao.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.nutricao.dto.request.PacienteUpdateRequestDTO;
import com.nuvexa.nutricao.dto.response.PacienteEnumsResponseDTO;
import com.nuvexa.nutricao.dto.response.PacienteResponseDTO;
import com.nuvexa.nutricao.service.PacienteService;
import com.nuvexa.platform.web.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
public class PacienteController extends BaseController {

    private final PacienteService pacienteService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PacienteResponseDTO> findAll(@RequestParam(required = false) String busca) {
        return pacienteService.findAll(busca);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PacienteResponseDTO findById(@PathVariable Long id) {
        return pacienteService.findById(id);
    }

    @GetMapping("/enums")
    @ResponseStatus(HttpStatus.OK)
    public PacienteEnumsResponseDTO getEnums() {
        return pacienteService.getEnums();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteResponseDTO create(@Valid @RequestBody PacienteCreateRequestDTO request) {
        return pacienteService.create(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PacienteResponseDTO update(@PathVariable Long id, @Valid @RequestBody PacienteUpdateRequestDTO request) {
        return pacienteService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        pacienteService.delete(id);
    }
}
