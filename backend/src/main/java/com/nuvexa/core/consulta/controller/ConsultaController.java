package com.nuvexa.core.consulta.controller;

import com.nuvexa.core.consulta.dto.request.ConsultaCreateRequestDTO;
import com.nuvexa.core.consulta.dto.request.ConsultaUpdateRequestDTO;
import com.nuvexa.core.consulta.dto.response.ConsultaResponseDTO;
import com.nuvexa.core.consulta.service.ConsultaService;
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
@RequestMapping("/api/v1/consultas")
@RequiredArgsConstructor
public class ConsultaController {

    private final ConsultaService consultaService;

    @GetMapping
    public List<ConsultaResponseDTO> findAll(@RequestParam(required = false) Long pacienteId) {
        return consultaService.findAll(pacienteId);
    }

    @GetMapping("/{id}")
    public ConsultaResponseDTO findById(@PathVariable Long id) {
        return consultaService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultaResponseDTO create(@Valid @RequestBody ConsultaCreateRequestDTO request) {
        return consultaService.create(request);
    }

    @PutMapping("/{id}")
    public ConsultaResponseDTO update(@PathVariable Long id, @Valid @RequestBody ConsultaUpdateRequestDTO request) {
        return consultaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        consultaService.delete(id);
    }
}
