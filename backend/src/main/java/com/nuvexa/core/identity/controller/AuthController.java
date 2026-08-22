package com.nuvexa.core.identity.controller;

import com.nuvexa.core.identity.dto.request.ForgotPasswordRequestDTO;
import com.nuvexa.core.identity.dto.request.LoginRequestDTO;
import com.nuvexa.core.identity.dto.request.RegisterRequestDTO;
import com.nuvexa.core.identity.dto.request.ResetPasswordRequestDTO;
import com.nuvexa.core.identity.dto.response.AuthResponseDTO;
import com.nuvexa.core.identity.dto.response.MessageResponseDTO;
import com.nuvexa.core.identity.service.AuthService;
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
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public MessageResponseDTO forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public MessageResponseDTO resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        return authService.resetPassword(request);
    }
}
