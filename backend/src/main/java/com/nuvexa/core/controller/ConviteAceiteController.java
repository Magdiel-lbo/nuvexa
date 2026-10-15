package com.nuvexa.core.controller;

import com.nuvexa.core.dto.request.ConviteTokenRequestDTO;
import com.nuvexa.core.dto.response.OrganizacaoDoUsuarioResponseDTO;
import com.nuvexa.core.service.ConviteService;
import com.nuvexa.platform.web.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Aceite de convite por quem JÁ tem conta: exige login e que o e-mail do convite seja o da conta. */
@RestController
@RequestMapping("/api/v1/convites")
@RequiredArgsConstructor
public class ConviteAceiteController extends BaseController {

    private final ConviteService conviteService;

    @PostMapping("/aceitar")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizacaoDoUsuarioResponseDTO aceitar(@Valid @RequestBody ConviteTokenRequestDTO request) {
        return conviteService.aceitarComoUsuarioLogado(request);
    }
}
