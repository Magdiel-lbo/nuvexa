package com.nuvexa.nutricao.controller;

import com.nuvexa.nutricao.dto.request.PlanoAlimentarCreateRequestDTO;
import com.nuvexa.nutricao.dto.request.PlanoAlimentarUpdateRequestDTO;
import com.nuvexa.nutricao.dto.response.PlanoAlimentarEnumsResponseDTO;
import com.nuvexa.nutricao.dto.response.PlanoAlimentarResponseDTO;
import com.nuvexa.nutricao.service.PlanoAlimentarService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/planos-alimentares")
@RequiredArgsConstructor
public class PlanoAlimentarController extends BaseController {

    private final PlanoAlimentarService planoAlimentarService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PlanoAlimentarResponseDTO> findAll(@RequestParam(required = false) Long pacienteId,
                                                     @RequestParam(required = false) String busca) {
        return planoAlimentarService.findAll(pacienteId, busca);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PlanoAlimentarResponseDTO findById(@PathVariable Long id) {
        return planoAlimentarService.findById(id);
    }

    @GetMapping("/enums")
    @ResponseStatus(HttpStatus.OK)
    public PlanoAlimentarEnumsResponseDTO getEnums() {
        return planoAlimentarService.getEnums();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanoAlimentarResponseDTO create(@Valid @RequestBody PlanoAlimentarCreateRequestDTO request) {
        return planoAlimentarService.create(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PlanoAlimentarResponseDTO update(@PathVariable Long id, @Valid @RequestBody PlanoAlimentarUpdateRequestDTO request) {
        return planoAlimentarService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        planoAlimentarService.delete(id);
    }
}
