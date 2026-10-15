package com.nuvexa.core.controller;

import com.nuvexa.core.dto.request.OrganizacaoUpdateRequestDTO;
import com.nuvexa.core.dto.response.OrganizacaoResponseDTO;
import com.nuvexa.core.service.OrganizacaoService;
import com.nuvexa.platform.web.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Dados da organização atual: o dono edita a própria empresa (o ADMIN usa /api/v1/organizacoes). */
@RestController
@RequestMapping("/api/v1/minha-organizacao")
@RequiredArgsConstructor
@PreAuthorize("@permissoes.pode('GERENCIAR_ORGANIZACAO')")
public class MinhaOrganizacaoController extends BaseController {

    private final OrganizacaoService organizacaoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public OrganizacaoResponseDTO atual() {
        return organizacaoService.atual();
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public OrganizacaoResponseDTO atualizar(@Valid @RequestBody OrganizacaoUpdateRequestDTO request) {
        return organizacaoService.atualizarAtual(request);
    }
}
