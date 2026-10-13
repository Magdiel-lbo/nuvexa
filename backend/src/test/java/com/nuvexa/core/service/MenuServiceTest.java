package com.nuvexa.core.service;

import com.nuvexa.core.dto.response.MenuItemDTO;
import com.nuvexa.core.model.Especialidade;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.platform.config.MenuProperties;
import com.nuvexa.platform.exception.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private ContextoDeAutenticacao contextoDeAutenticacao;

    private MenuService menuService;

    @BeforeEach
    void montarMenu() {
        MenuProperties.Item dashboard = item("dashboard", "/", null);
        MenuProperties.Item consultas = item("consultas", "/consultas", null);
        MenuProperties.Item nutricao = item("nutricao", "/nutricao", Especialidade.NUTRICAO);
        MenuProperties.Item atendimento = item("atendimento", null, null);
        atendimento.setChildren(List.of(consultas, nutricao));
        MenuProperties.Item soNutricao = item("so-nutricao", null, null);
        soNutricao.setChildren(List.of(item("avaliacoes", "/avaliacoes", Especialidade.NUTRICAO)));

        MenuProperties propriedades = new MenuProperties();
        propriedades.setItens(List.of(dashboard, atendimento, soNutricao));
        menuService = new MenuService(propriedades, contextoDeAutenticacao);
    }

    private MenuProperties.Item item(String id, String rota, Especialidade especialidade) {
        MenuProperties.Item item = new MenuProperties.Item();
        item.setId(id);
        item.setRoute(rota);
        if (especialidade != null) {
            item.setEspecialidades(List.of(especialidade.name()));
        }
        return item;
    }

    private void organizacaoDa(Especialidade especialidade) {
        Organizacao organizacao = Organizacao.builder().especialidade(especialidade).build();
        when(contextoDeAutenticacao.organizacaoAtual()).thenReturn(organizacao);
    }

    @Test
    void organizacaoDeNutricaoVeTodosOsItens() {
        organizacaoDa(Especialidade.NUTRICAO);

        List<MenuItemDTO> menu = menuService.listar();

        assertThat(menu).extracting(MenuItemDTO::getId).containsExactly("dashboard", "atendimento", "so-nutricao");
        assertThat(menu.get(1).getChildren()).extracting(MenuItemDTO::getId).containsExactly("consultas", "nutricao");
    }

    @Test
    void itemRestritoAOutraEspecialidadeNaoApareceEGrupoVazioSome() {
        // Especialidade nula = organização sem especialidade resolvida: só passam itens sem restrição.
        organizacaoDa(null);

        List<MenuItemDTO> menu = menuService.listar();

        assertThat(menu).extracting(MenuItemDTO::getId).containsExactly("dashboard", "atendimento");
        assertThat(menu.get(1).getChildren()).extracting(MenuItemDTO::getId).containsExactly("consultas");
    }

    @Test
    void semVinculoAtivoSoVeItensSemRestricao() {
        when(contextoDeAutenticacao.organizacaoAtual())
                .thenThrow(new NegocioException(HttpStatus.FORBIDDEN, "sem vínculo"));

        List<MenuItemDTO> menu = menuService.listar();

        assertThat(menu).extracting(MenuItemDTO::getId).containsExactly("dashboard", "atendimento");
        assertThat(menu.get(1).getChildren()).extracting(MenuItemDTO::getId).containsExactly("consultas");
    }
}
