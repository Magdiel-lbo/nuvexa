package com.nuvexa.verticals.nutrition.controller;

import com.nuvexa.verticals.nutrition.dto.request.PatientCreateRequestDTO;
import com.nuvexa.verticals.nutrition.dto.request.PatientUpdateRequestDTO;
import com.nuvexa.verticals.nutrition.dto.response.PatientEnumsResponseDTO;
import com.nuvexa.verticals.nutrition.dto.response.PatientResponseDTO;
import com.nuvexa.verticals.nutrition.service.PatientService;
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
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public List<PatientResponseDTO> findAll(@RequestParam(required = false) String search) {
        return patientService.findAll(search);
    }

    @GetMapping("/{id}")
    public PatientResponseDTO findById(@PathVariable Long id) {
        return patientService.findById(id);
    }

    @GetMapping("/enums")
    public PatientEnumsResponseDTO getEnums() {
        return patientService.getEnums();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponseDTO create(@Valid @RequestBody PatientCreateRequestDTO request) {
        return patientService.create(request);
    }

    @PutMapping("/{id}")
    public PatientResponseDTO update(@PathVariable Long id, @Valid @RequestBody PatientUpdateRequestDTO request) {
        return patientService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        patientService.delete(id);
    }
}
