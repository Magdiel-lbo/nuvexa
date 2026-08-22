package com.nuvexa.nucleo.identidade.controller;

import com.nuvexa.nucleo.identidade.dto.request.EsqueciSenhaRequestDTO;
import com.nuvexa.nucleo.identidade.dto.request.LoginRequestDTO;
import com.nuvexa.nucleo.identidade.dto.request.CadastroRequestDTO;
import com.nuvexa.nucleo.identidade.dto.request.RedefinirSenhaRequestDTO;
import com.nuvexa.nucleo.identidade.dto.response.AutenticacaoResponseDTO;
import com.nuvexa.nucleo.identidade.dto.response.MensagemResponseDTO;
import com.nuvexa.nucleo.identidade.service.AutenticacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AutenticacaoResponseDTO register(@Valid @RequestBody CadastroRequestDTO request) {
        return autenticacaoService.register(request);
    }

    @PostMapping("/login")
    public AutenticacaoResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return autenticacaoService.login(request);
    }

    @PostMapping("/forgot-password")
    public MensagemResponseDTO forgotPassword(@Valid @RequestBody EsqueciSenhaRequestDTO request) {
        return autenticacaoService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public MensagemResponseDTO resetPassword(@Valid @RequestBody RedefinirSenhaRequestDTO request) {
        return autenticacaoService.resetPassword(request);
    }
}
