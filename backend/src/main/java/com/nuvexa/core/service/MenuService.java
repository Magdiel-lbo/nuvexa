package com.nuvexa.core.service;

import com.nuvexa.core.dto.response.MenuItemDTO;
import com.nuvexa.core.model.Especialidade;
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

    /**
     * Árvore de navegação da organização atual: itens com {@code especialidades} só aparecem para
     * quem tem uma delas habilitada. É conveniência de navegação — o acesso de fato é barrado no
     * backend pelo portão de especialidade dos controllers de cada vertical.
     */
    @Transactional(readOnly = true)
    public List<MenuItemDTO> listar() {
        return mapear(menuProperties.getItens(), especialidadeAtual());
    }

    private Especialidade especialidadeAtual() {
        try {
            return contextoDeAutenticacao.organizacaoAtual().getEspecialidade();
        } catch (NegocioException ex) {
            // Sem vínculo ativo: só os itens que não dependem de especialidade.
            return null;
        }
    }

    private List<MenuItemDTO> mapear(List<MenuProperties.Item> itens, Especialidade especialidade) {
        return itens.stream()
                .filter(item -> visivel(item, especialidade))
                .map(item -> MenuItemDTO.builder()
                        .id(item.getId())
                        .labelKey(item.getLabelKey())
                        .icon(item.getIcon())
                        .route(item.getRoute())
                        .children(mapear(item.getChildren(), especialidade))
                        .build())
                // Grupo cujos filhos foram todos filtrados some junto, em vez de aparecer vazio.
                .filter(item -> item.getRoute() != null || !item.getChildren().isEmpty())
                .toList();
    }

    private boolean visivel(MenuProperties.Item item, Especialidade especialidade) {
        return item.getEspecialidades().isEmpty()
                || (especialidade != null && item.getEspecialidades().contains(especialidade.name()));
    }
}
