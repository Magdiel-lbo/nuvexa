package com.nuvexa.core.contexto.controller;

import com.nuvexa.core.contexto.dto.response.ContextoResponseDTO;
import com.nuvexa.core.contexto.service.ContextoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contexto")
@RequiredArgsConstructor
public class ContextoController {

    private final ContextoService contextoService;

    @GetMapping
    public ContextoResponseDTO atual() {
        return contextoService.atual();
    }
}
