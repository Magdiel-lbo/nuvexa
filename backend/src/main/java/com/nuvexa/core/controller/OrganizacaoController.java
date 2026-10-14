package com.nuvexa.core.controller;

import com.nuvexa.core.dto.request.OrganizacaoCreateRequestDTO;
import com.nuvexa.core.dto.request.OrganizacaoUpdateRequestDTO;
import com.nuvexa.core.dto.response.OrganizacaoEnumsResponseDTO;
import com.nuvexa.core.dto.response.OrganizacaoResponseDTO;
import com.nuvexa.core.service.OrganizacaoService;
import com.nuvexa.platform.web.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

/** Cadastro de empresas — exclusivo do papel de plataforma ADMIN. */
@RestController
@RequestMapping("/api/v1/organizacoes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OrganizacaoController extends BaseController {

    private final OrganizacaoService organizacaoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrganizacaoResponseDTO> findAll(@RequestParam(required = false) String busca) {
        return organizacaoService.findAll(busca);
    }

    @GetMapping("/enums")
    @ResponseStatus(HttpStatus.OK)
    public OrganizacaoEnumsResponseDTO getEnums() {
        return organizacaoService.getEnums();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrganizacaoResponseDTO findById(@PathVariable Long id) {
        return organizacaoService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizacaoResponseDTO create(@Valid @RequestBody OrganizacaoCreateRequestDTO request) {
        return organizacaoService.create(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrganizacaoResponseDTO update(@PathVariable Long id, @Valid @RequestBody OrganizacaoUpdateRequestDTO request) {
        return organizacaoService.update(id, request);
    }
}
