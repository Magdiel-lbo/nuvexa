package com.nuvexa.core.service;

import com.nuvexa.core.dto.response.MenuItemDTO;
import com.nuvexa.platform.config.MenuProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuProperties menuProperties;

    public List<MenuItemDTO> listar() {
        return mapear(menuProperties.getItens());
    }

    private List<MenuItemDTO> mapear(List<MenuProperties.Item> itens) {
        return itens.stream()
                .map(item -> MenuItemDTO.builder()
                        .id(item.getId())
                        .labelKey(item.getLabelKey())
                        .icon(item.getIcon())
                        .route(item.getRoute())
                        .children(mapear(item.getChildren()))
                        .build())
                .toList();
    }
}
