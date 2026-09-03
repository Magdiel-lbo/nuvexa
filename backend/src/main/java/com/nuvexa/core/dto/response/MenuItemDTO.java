package com.nuvexa.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Nó da árvore de navegação: um grupo (com {@code children}) ou um item navegável (com
 * {@code route}). O frontend só renderiza essa estrutura — quem decide o que existe é o
 * {@code menu.yml} do backend.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemDTO {

    private String id;

    private String labelKey;

    private String icon;

    private String route;

    private List<MenuItemDTO> children;
}
