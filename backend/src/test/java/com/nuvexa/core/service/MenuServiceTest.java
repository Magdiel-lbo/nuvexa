package com.nuvexa.core.service;

import com.nuvexa.core.dto.response.MenuItemDTO;
import com.nuvexa.core.model.Especialidade;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.PapelOrganizacional;
import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
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
        MenuProperties.Item dashboard = item("dashboard", "/");
        MenuProperties.Item consultas = item("consultas", "/consultas");
        MenuProperties.Item prontuarios = item("prontuarios", "/prontuarios");
        prontuarios.setPermissoes(List.of("CLINICO"));
        MenuProperties.Item nutricao = item("nutricao", "/nutricao");
        nutricao.setEspecialidades(List.of("NUTRICAO"));
        MenuProperties.Item atendimento = item("atendimento", null);
        atendimento.setChildren(List.of(consultas, prontuarios, nutricao));

        MenuProperties.Item avaliacoes = item("avaliacoes", "/avaliacoes");
        avaliacoes.setEspecialidades(List.of("NUTRICAO"));
        MenuProperties.Item soNutricao = item("so-nutricao", null);
        soNutricao.setChildren(List.of(avaliacoes));

        MenuProperties.Item empresas = item("empresas", "/empresa");
        empresas.setPerfis(List.of("ADMIN"));
        MenuProperties.Item equipe = item("equipe", "/equipe");
        equipe.setPermissoes(List.of("GERENCIAR_EQUIPE"));
        MenuProperties.Item gestao = item("gestao", null);
        gestao.setChildren(List.of(empresas, equipe));

        MenuProperties propriedades = new MenuProperties();
        propriedades.setItens(List.of(dashboard, atendimento, soNutricao, gestao));
        menuService = new MenuService(propriedades, contextoDeAutenticacao);
    }

    private MenuProperties.Item item(String id, String rota) {
        MenuProperties.Item item = new MenuProperties.Item();
        item.setId(id);
        item.setRoute(rota);
        return item;
    }

    private void logadoComo(Perfil perfil, PapelOrganizacional papel, Especialidade especialidade) {
        Usuario usuario = Usuario.builder().perfil(perfil).build();
        Organizacao organizacao = Organizacao.builder().especialidade(especialidade).build();
        when(contextoDeAutenticacao.usuarioAtual()).thenReturn(usuario);
        when(contextoDeAutenticacao.vinculoAtual()).thenReturn(Vinculo.builder().usuario(usuario).organizacao(organizacao).papel(papel).ativo(true).build());
    }

    private List<String> filhosDe(List<MenuItemDTO> menu, String grupo) {
        return menu.stream().filter(item -> item.getId().equals(grupo)).findFirst().orElseThrow()
                .getChildren().stream().map(MenuItemDTO::getId).toList();
    }

    @Test
    void proprietarioDeNutricaoVeTudoMenosOQueEExclusivoDeAdmin() {
        logadoComo(Perfil.PROFISSIONAL, PapelOrganizacional.PROPRIETARIO, Especialidade.NUTRICAO);

        List<MenuItemDTO> menu = menuService.listar();

        assertThat(menu).extracting(MenuItemDTO::getId).containsExactly("dashboard", "atendimento", "so-nutricao", "gestao");
        assertThat(filhosDe(menu, "atendimento")).containsExactly("consultas", "prontuarios", "nutricao");
        assertThat(filhosDe(menu, "gestao")).containsExactly("equipe");
    }

    @Test
    void adminVeOItemDeEmpresasEPerfilProfissionalNao() {
        logadoComo(Perfil.ADMIN, PapelOrganizacional.MEMBRO, Especialidade.NUTRICAO);

        assertThat(filhosDe(menuService.listar(), "gestao")).containsExactly("empresas");
    }

    @Test
    void recepcaoNaoVeItensClinicosNemDeGestaoDeEquipe() {
        logadoComo(Perfil.PROFISSIONAL, PapelOrganizacional.RECEPCAO, Especialidade.NUTRICAO);

        List<MenuItemDTO> menu = menuService.listar();

        assertThat(filhosDe(menu, "atendimento")).containsExactly("consultas", "nutricao");
        assertThat(menu).extracting(MenuItemDTO::getId).doesNotContain("gestao");
    }

    @Test
    void gestorVeEquipeEMembroNao() {
        logadoComo(Perfil.PROFISSIONAL, PapelOrganizacional.GESTOR, Especialidade.NUTRICAO);
        assertThat(filhosDe(menuService.listar(), "gestao")).containsExactly("equipe");

        logadoComo(Perfil.PROFISSIONAL, PapelOrganizacional.MEMBRO, Especialidade.NUTRICAO);
        assertThat(menuService.listar()).extracting(MenuItemDTO::getId).doesNotContain("gestao");
    }

    @Test
    void organizacaoSemEspecialidadeResolvidaSoPassaItensSemRestricaoDeEspecialidade() {
        logadoComo(Perfil.PROFISSIONAL, PapelOrganizacional.MEMBRO, null);

        List<MenuItemDTO> menu = menuService.listar();

        assertThat(menu).extracting(MenuItemDTO::getId).containsExactly("dashboard", "atendimento");
        assertThat(filhosDe(menu, "atendimento")).containsExactly("consultas", "prontuarios");
    }

    @Test
    void semVinculoAtivoSoVeItensSemRestricaoDeOrganizacao() {
        when(contextoDeAutenticacao.usuarioAtual()).thenReturn(Usuario.builder().perfil(Perfil.PROFISSIONAL).build());
        when(contextoDeAutenticacao.vinculoAtual()).thenThrow(new NegocioException(HttpStatus.FORBIDDEN, "sem vínculo"));

        List<MenuItemDTO> menu = menuService.listar();

        assertThat(menu).extracting(MenuItemDTO::getId).containsExactly("dashboard", "atendimento");
        assertThat(filhosDe(menu, "atendimento")).containsExactly("consultas");
    }
}
