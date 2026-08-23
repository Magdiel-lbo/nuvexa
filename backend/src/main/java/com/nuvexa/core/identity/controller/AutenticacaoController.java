package com.nuvexa.core.identity.controller;

import com.nuvexa.core.identity.dto.request.EsqueciSenhaRequestDTO;
import com.nuvexa.core.identity.dto.request.LoginRequestDTO;
import com.nuvexa.core.identity.dto.request.CadastroRequestDTO;
import com.nuvexa.core.identity.dto.request.RedefinirSenhaRequestDTO;
import com.nuvexa.core.identity.dto.response.AutenticacaoResponseDTO;
import com.nuvexa.core.identity.dto.response.MensagemResponseDTO;
import com.nuvexa.core.identity.service.AutenticacaoService;
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

    @PostMapping("/cadastrar")
    @ResponseStatus(HttpStatus.CREATED)
    public AutenticacaoResponseDTO cadastrar(@Valid @RequestBody CadastroRequestDTO request) {
        return autenticacaoService.cadastrar(request);
    }

    @PostMapping("/login")
    public AutenticacaoResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return autenticacaoService.login(request);
    }

    @PostMapping("/esqueci-senha")
    public MensagemResponseDTO esqueciSenha(@Valid @RequestBody EsqueciSenhaRequestDTO request) {
        return autenticacaoService.esqueciSenha(request);
    }

    @PostMapping("/redefinir-senha")
    public MensagemResponseDTO redefinirSenha(@Valid @RequestBody RedefinirSenhaRequestDTO request) {
        return autenticacaoService.redefinirSenha(request);
    }
}
