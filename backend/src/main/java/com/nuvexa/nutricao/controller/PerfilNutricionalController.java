package com.nuvexa.nutricao.controller;

import com.nuvexa.nutricao.dto.request.PerfilNutricionalCreateRequestDTO;
import com.nuvexa.nutricao.dto.request.PerfilNutricionalUpdateRequestDTO;
import com.nuvexa.nutricao.dto.response.PerfilNutricionalEnumsResponseDTO;
import com.nuvexa.nutricao.dto.response.PerfilNutricionalResponseDTO;
import com.nuvexa.nutricao.service.PerfilNutricionalService;
import com.nuvexa.platform.web.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PerfilNutricionalController extends BaseController {

    private final PerfilNutricionalService perfilNutricionalService;

    @GetMapping("/api/v1/perfis-nutricionais")
    @ResponseStatus(HttpStatus.OK)
    public List<PerfilNutricionalResponseDTO> findAll(@RequestParam(required = false) String busca) {
        return perfilNutricionalService.findAll(busca);
    }

    @GetMapping("/api/v1/pacientes/{pacienteId}/perfil-nutricional")
    @ResponseStatus(HttpStatus.OK)
    public PerfilNutricionalResponseDTO findByPacienteId(@PathVariable Long pacienteId) {
        return perfilNutricionalService.findByPacienteId(pacienteId);
    }

    @GetMapping("/api/v1/perfis-nutricionais/enums")
    @ResponseStatus(HttpStatus.OK)
    public PerfilNutricionalEnumsResponseDTO getEnums() {
        return perfilNutricionalService.getEnums();
    }

    @PostMapping("/api/v1/pacientes/{pacienteId}/perfil-nutricional")
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilNutricionalResponseDTO create(@PathVariable Long pacienteId, @Valid @RequestBody PerfilNutricionalCreateRequestDTO request) {
        return perfilNutricionalService.create(pacienteId, request);
    }

    @PutMapping("/api/v1/pacientes/{pacienteId}/perfil-nutricional")
    @ResponseStatus(HttpStatus.OK)
    public PerfilNutricionalResponseDTO update(@PathVariable Long pacienteId, @Valid @RequestBody PerfilNutricionalUpdateRequestDTO request) {
        return perfilNutricionalService.update(pacienteId, request);
    }

    @DeleteMapping("/api/v1/pacientes/{pacienteId}/perfil-nutricional")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long pacienteId) {
        perfilNutricionalService.delete(pacienteId);
    }
}
