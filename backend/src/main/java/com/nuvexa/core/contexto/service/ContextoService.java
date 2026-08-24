package com.nuvexa.core.contexto.service;

import com.nuvexa.core.contexto.ContextoDeAutenticacao;
import com.nuvexa.core.contexto.dto.response.ContextoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContextoService {

    private final ContextoDeAutenticacao contextoDeAutenticacao;

    @Transactional(readOnly = true)
    public ContextoResponseDTO atual() {
        return ContextoResponseDTO.from(contextoDeAutenticacao.vinculoAtual());
    }
}
