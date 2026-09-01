package com.nuvexa.core.controller;

import com.nuvexa.core.dto.response.ContextoResponseDTO;
import com.nuvexa.core.service.ContextoService;
import com.nuvexa.platform.web.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contexto")
@RequiredArgsConstructor
public class ContextoController extends BaseController {

    private final ContextoService contextoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ContextoResponseDTO atual() {
        return contextoService.atual();
    }
}
