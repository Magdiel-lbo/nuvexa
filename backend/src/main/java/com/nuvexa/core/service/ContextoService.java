package com.nuvexa.core.service;

import com.nuvexa.core.service.ContextoDeAutenticacao;
import com.nuvexa.core.dto.response.ContextoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Não é delegação pura: o @Transactional aqui é o que mantém a sessão do Hibernate aberta entre
 * a busca do vínculo (ContextoDeAutenticacao) e o acesso aos relacionamentos @ManyToOne (lazy)
 * de Usuario/Organizacao dentro de ContextoResponseDTO.from(...). Sem essa fronteira — por
 * exemplo, se o controller chamasse ContextoDeAutenticacao direto, sem transação — o mapeamento
 * do DTO lança LazyInitializationException (confirmado empiricamente). Por isso o service
 * continua existindo mesmo tendo um único método de uma linha.
 */
@Service
@RequiredArgsConstructor
public class ContextoService {

    private final ContextoDeAutenticacao contextoDeAutenticacao;

    @Transactional(readOnly = true)
    public ContextoResponseDTO atual() {
        return ContextoResponseDTO.from(contextoDeAutenticacao.vinculoAtual());
    }
}
