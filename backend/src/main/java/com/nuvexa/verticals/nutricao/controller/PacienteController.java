package com.nuvexa.verticals.nutricao.controller;

import com.nuvexa.verticals.nutricao.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.verticals.nutricao.dto.request.PacienteUpdateRequestDTO;
import com.nuvexa.verticals.nutricao.dto.response.PacienteEnumsResponseDTO;
import com.nuvexa.verticals.nutricao.dto.response.PacienteResponseDTO;
import com.nuvexa.verticals.nutricao.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping
    public List<PacienteResponseDTO> findAll(@RequestParam(required = false) String search) {
        return pacienteService.findAll(search);
    }

    @GetMapping("/{id}")
    public PacienteResponseDTO findById(@PathVariable Long id) {
        return pacienteService.findById(id);
    }

    @GetMapping("/enums")
    public PacienteEnumsResponseDTO getEnums() {
        return pacienteService.getEnums();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteResponseDTO create(@Valid @RequestBody PacienteCreateRequestDTO request) {
        return pacienteService.create(request);
    }

    @PutMapping("/{id}")
    public PacienteResponseDTO update(@PathVariable Long id, @Valid @RequestBody PacienteUpdateRequestDTO request) {
        return pacienteService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        pacienteService.delete(id);
    }
}
