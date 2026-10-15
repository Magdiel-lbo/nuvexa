package com.nuvexa.core.controller;

import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.OrganizacaoRepository;
import com.nuvexa.core.repository.UsuarioRepository;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.testsupport.LimpezaDeUsuarios;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prova a fiação do portão de especialidade com o contexto completo e SEM transação ao redor do
 * teste (diferente dos {@code @DataJpaTest}): o {@code @PreAuthorize} dos controllers de nutrição
 * é avaliado pelo Spring Security real, com {@code open-in-view} desligado, como em produção.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EspecialidadeAutorizacaoIntegrationTest {

    private static final String EMAIL = "especialidade.e2e@nuvexa.test";

    private static final List<String> LISTAGENS_DE_NUTRICAO = List.of(
            "/api/v1/avaliacoes",
            "/api/v1/avaliacoes/relatorio",
            "/api/v1/planos-alimentares",
            "/api/v1/planos-alimentares/relatorio",
            "/api/v1/perfis-nutricionais",
            "/api/v1/pacientes/relatorio");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VinculoRepository vinculoRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbc;

    @AfterEach
    void limparUsuarioCriado() {
        usuarioRepository.findByEmail(EMAIL).ifPresent(usuario -> {
            List<Vinculo> vinculos = vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(usuario.getId());
            vinculoRepository.deleteAll(vinculos);
            vinculos.forEach(vinculo -> organizacaoRepository.deleteById(vinculo.getOrganizacao().getId()));
            LimpezaDeUsuarios.removerEventosDe(jdbc, usuario.getId());
            usuarioRepository.delete(usuario);
        });
    }

    @Test
    void organizacaoDeNutricaoAcessaEndpointsDeNutricaoEVeOMenuDeNutricao() throws Exception {
        String resposta = mockMvc.perform(post("/api/v1/auth/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teste E2E\",\"email\":\"" + EMAIL + "\",\"senha\":\"senha12345\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String authorization = "Bearer " + JsonPath.read(resposta, "$.token");

        mockMvc.perform(get("/api/v1/contexto").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especialidade").value("NUTRICAO"));

        mockMvc.perform(get("/api/v1/menus").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..id", hasItem("nutricao")))
                .andExpect(jsonPath("$..id", hasItem("avaliacoes")));

        for (String endpoint : LISTAGENS_DE_NUTRICAO) {
            mockMvc.perform(get(endpoint).header("Authorization", authorization))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void endpointsDeNutricaoDevemSerNegadosSemOrganizacaoComAEspecialidade() throws Exception {
        // Principal sem vínculo: nenhuma organização atual existe, então o portão nega (403).
        for (String endpoint : LISTAGENS_DE_NUTRICAO) {
            mockMvc.perform(get(endpoint).with(user("prof").roles("PROFISSIONAL")))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void endpointsDeNutricaoContinuamExigindoAutenticacao() throws Exception {
        for (String endpoint : LISTAGENS_DE_NUTRICAO) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isUnauthorized());
        }
    }
}
