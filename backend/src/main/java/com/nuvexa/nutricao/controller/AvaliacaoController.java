package com.nuvexa.nutricao.controller;

import com.nuvexa.nutricao.dto.request.AvaliacaoCreateRequestDTO;
import com.nuvexa.nutricao.dto.request.AvaliacaoUpdateRequestDTO;
import com.nuvexa.nutricao.dto.response.AvaliacaoEnumsResponseDTO;
import com.nuvexa.nutricao.dto.response.AvaliacaoResponseDTO;
import com.nuvexa.nutricao.service.AvaliacaoService;
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
@RequestMapping("/api/v1/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController extends BaseController {

    private final AvaliacaoService avaliacaoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AvaliacaoResponseDTO> findAll(@RequestParam(required = false) Long pacienteId,
                                               @RequestParam(required = false) String busca) {
        return avaliacaoService.findAll(pacienteId, busca);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AvaliacaoResponseDTO findById(@PathVariable Long id) {
        return avaliacaoService.findById(id);
    }

    @GetMapping("/enums")
    @ResponseStatus(HttpStatus.OK)
    public AvaliacaoEnumsResponseDTO getEnums() {
        return avaliacaoService.getEnums();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvaliacaoResponseDTO create(@Valid @RequestBody AvaliacaoCreateRequestDTO request) {
        return avaliacaoService.create(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AvaliacaoResponseDTO update(@PathVariable Long id, @Valid @RequestBody AvaliacaoUpdateRequestDTO request) {
        return avaliacaoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        avaliacaoService.delete(id);
    }
}
