package com.nuvexa.core.service;

import com.nuvexa.core.dto.response.MenuItemDTO;
import com.nuvexa.core.model.Especialidade;
import com.nuvexa.core.model.Permissao;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.platform.config.MenuProperties;
import com.nuvexa.platform.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuProperties menuProperties;
    private final ContextoDeAutenticacao contextoDeAutenticacao;

    /** O que filtra o menu para a requisição atual; qualquer campo pode faltar (sem vínculo ativo). */
    private record Visao(Usuario usuario, Vinculo vinculo, Especialidade especialidade) {
    }

    /**
     * Árvore de navegação da requisição atual: itens podem exigir especialidade da organização,
     * permissão do papel ou perfil de plataforma. É conveniência de navegação — o acesso de fato é
     * barrado no backend pelos {@code @PreAuthorize} dos controllers.
     */
    @Transactional(readOnly = true)
    public List<MenuItemDTO> listar() {
        return mapear(menuProperties.getItens(), visaoAtual());
    }

    private Visao visaoAtual() {
        Usuario usuario = contextoDeAutenticacao.usuarioAtual();
        try {
            Vinculo vinculo = contextoDeAutenticacao.vinculoAtual();
            return new Visao(usuario, vinculo, vinculo.getOrganizacao().getEspecialidade());
        } catch (NegocioException ex) {
            // Sem vínculo ativo: só os itens que não dependem de organização.
            return new Visao(usuario, null, null);
        }
    }

    private List<MenuItemDTO> mapear(List<MenuProperties.Item> itens, Visao visao) {
        return itens.stream()
                .filter(item -> visivel(item, visao))
                .map(item -> MenuItemDTO.builder()
                        .id(item.getId())
                        .labelKey(item.getLabelKey())
                        .icon(item.getIcon())
                        .route(item.getRoute())
                        .children(mapear(item.getChildren(), visao))
                        .build())
                // Grupo cujos filhos foram todos filtrados some junto, em vez de aparecer vazio.
                .filter(item -> item.getRoute() != null || !item.getChildren().isEmpty())
                .toList();
    }

    private boolean visivel(MenuProperties.Item item, Visao visao) {
        boolean especialidadeOk = item.getEspecialidades().isEmpty()
                || (visao.especialidade() != null && item.getEspecialidades().contains(visao.especialidade().name()));
        boolean permissoesOk = item.getPermissoes().stream()
                .allMatch(nome -> visao.vinculo() != null && visao.vinculo().getPapel().possui(Permissao.valueOf(nome)));
        boolean perfilOk = item.getPerfis().isEmpty() || item.getPerfis().contains(visao.usuario().getPerfil().name());
        return especialidadeOk && permissoesOk && perfilOk;
    }
}
