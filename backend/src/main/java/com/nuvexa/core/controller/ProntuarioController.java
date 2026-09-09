package com.nuvexa.core.controller;

import com.nuvexa.core.dto.request.ProntuarioCreateRequestDTO;
import com.nuvexa.core.dto.request.ProntuarioUpdateRequestDTO;
import com.nuvexa.core.dto.response.ProntuarioEnumsResponseDTO;
import com.nuvexa.core.dto.response.ProntuarioResponseDTO;
import com.nuvexa.core.service.ProntuarioService;
import com.nuvexa.platform.auditoria.EventoAuditoriaResponseDTO;
import com.nuvexa.platform.web.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/v1/prontuarios")
@RequiredArgsConstructor
public class ProntuarioController extends BaseController {

    private final ProntuarioService prontuarioService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProntuarioResponseDTO> findAll(@RequestParam(required = false) Long pacienteId,
                                                @RequestParam(required = false) String busca) {
        return prontuarioService.findAll(pacienteId, busca);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProntuarioResponseDTO findById(@PathVariable Long id) {
        return prontuarioService.findById(id);
    }

    @GetMapping("/enums")
    @ResponseStatus(HttpStatus.OK)
    public ProntuarioEnumsResponseDTO getEnums() {
        return prontuarioService.getEnums();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProntuarioResponseDTO create(@Valid @RequestBody ProntuarioCreateRequestDTO request) {
        return prontuarioService.create(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProntuarioResponseDTO update(@PathVariable Long id, @Valid @RequestBody ProntuarioUpdateRequestDTO request) {
        return prontuarioService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        prontuarioService.delete(id);
    }

    @PatchMapping("/{id}/assinar")
    @ResponseStatus(HttpStatus.OK)
    public ProntuarioResponseDTO assinar(@PathVariable Long id) {
        return prontuarioService.assinar(id);
    }

    @GetMapping("/{id}/auditoria")
    @ResponseStatus(HttpStatus.OK)
    public List<EventoAuditoriaResponseDTO> listarAuditoria(@PathVariable Long id) {
        return prontuarioService.listarAuditoria(id);
    }
}
