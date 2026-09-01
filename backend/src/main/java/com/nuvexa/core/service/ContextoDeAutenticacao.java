package com.nuvexa.core.service;

import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.platform.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Ponto único de leitura do contexto da requisição autenticada: quem está chamando, em qual
 * organização e com qual papel organizacional. Existe para que nenhum service precise falar
 * com o {@link SecurityContextHolder} diretamente.
 *
 * <p>Só lê contexto — não aplica regra de negócio nem decide autorização de recurso; quem faz
 * isso é a própria query de cada service (escopo) e o Spring Security (papel de acesso).
 */
@Component
@RequiredArgsConstructor
public class ContextoDeAutenticacao {

    private static final Locale MESSAGE_LOCALE = Locale.of("pt", "BR");

    private final VinculoRepository vinculoRepository;
    private final MessageSource messageSource;

    public Usuario usuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new NegocioException(HttpStatus.UNAUTHORIZED, resolveMessage("contexto.naoAutenticado"));
        }
        return usuario;
    }

    /**
     * Vínculo ativo do usuário autenticado. Hoje cada usuário tem exatamente um; quando a troca
     * de organização existir, este é o ponto que passará a considerar a organização escolhida na
     * requisição, em vez de assumir o vínculo mais antigo.
     */
    public Vinculo vinculoAtual() {
        List<Vinculo> vinculos = vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(usuarioAtual().getId());

        if (vinculos.isEmpty()) {
            throw new NegocioException(HttpStatus.FORBIDDEN, resolveMessage("contexto.semVinculo"));
        }
        return vinculos.getFirst();
    }

    public Organizacao organizacaoAtual() {
        return vinculoAtual().getOrganizacao();
    }

    public Long organizacaoAtualId() {
        return organizacaoAtual().getId();
    }

    private String resolveMessage(String key) {
        return messageSource.getMessage(key, null, MESSAGE_LOCALE);
    }
}
