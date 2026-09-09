package com.nuvexa.core.controller;

import com.nuvexa.core.dto.request.ProntuarioAdendoCreateRequestDTO;
import com.nuvexa.core.dto.response.ProntuarioAdendoResponseDTO;
import com.nuvexa.core.service.ProntuarioAdendoService;
import com.nuvexa.platform.web.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prontuarios/{prontuarioId}/adendos")
@RequiredArgsConstructor
public class ProntuarioAdendoController extends BaseController {

    private final ProntuarioAdendoService prontuarioAdendoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProntuarioAdendoResponseDTO> findAll(@PathVariable Long prontuarioId) {
        return prontuarioAdendoService.findAll(prontuarioId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProntuarioAdendoResponseDTO create(@PathVariable Long prontuarioId, @Valid @RequestBody ProntuarioAdendoCreateRequestDTO request) {
        return prontuarioAdendoService.create(prontuarioId, request);
    }
}
