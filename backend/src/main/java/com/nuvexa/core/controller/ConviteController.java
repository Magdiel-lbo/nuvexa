package com.nuvexa.core.controller;

import com.nuvexa.core.dto.request.ConviteCreateRequestDTO;
import com.nuvexa.core.dto.response.ConviteResponseDTO;
import com.nuvexa.core.service.ConviteService;
import com.nuvexa.platform.web.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Convites da organização atual. O aceite é público e fica em {@code /api/v1/auth/convites/aceitar}. */
@RestController
@RequestMapping("/api/v1/convites")
@RequiredArgsConstructor
@PreAuthorize("@permissoes.pode('GERENCIAR_EQUIPE')")
public class ConviteController extends BaseController {

    private final ConviteService conviteService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ConviteResponseDTO> listar() {
        return conviteService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConviteResponseDTO convidar(@Valid @RequestBody ConviteCreateRequestDTO request) {
        return conviteService.convidar(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revogar(@PathVariable Long id) {
        conviteService.revogar(id);
    }
}
