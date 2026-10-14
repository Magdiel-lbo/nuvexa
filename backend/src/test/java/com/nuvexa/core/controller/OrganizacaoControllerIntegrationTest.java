package com.nuvexa.core.controller;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.repository.OrganizacaoRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cadastro de empresas com o contexto completo (SecurityConfig e @PreAuthorize reais, banco real):
 * só ADMIN opera; PROFISSIONAL recebe 403 e anônimo 401.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrganizacaoControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/organizacoes";
    private static final String DOCUMENTO = "99.999.999/0001-99";
    private static final String NOME = "Clínica Teste Gestão";

    private static final String CORPO_VALIDO = """
            {"nome":"%s","razaoSocial":"Clínica Teste LTDA","documento":"%s","tipo":"CLINICA","especialidade":"NUTRICAO"}
            """.formatted(NOME, DOCUMENTO);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @AfterEach
    void limpar() {
        List<Organizacao> criadas = organizacaoRepository.buscar(NOME);
        organizacaoRepository.deleteAll(criadas);
    }

    @Test
    void adminCriaConsultaEEditaEmpresa() throws Exception {
        mockMvc.perform(post(BASE_URL).with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(NOME))
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andExpect(jsonPath("$.especialidade").value("NUTRICAO"));

        Long id = organizacaoRepository.buscar(NOME).getFirst().getId();

        mockMvc.perform(get(BASE_URL + "?busca=gestão").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id.intValue())));

        mockMvc.perform(put(BASE_URL + "/" + id).with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + NOME + "\",\"razaoSocial\":\"Nova Razão\",\"documento\":\"" + DOCUMENTO + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Nova Razão"))
                .andExpect(jsonPath("$.tipo").value("CLINICA"));
    }

    @Test
    void documentoDuplicadoRetornaConflito() throws Exception {
        mockMvc.perform(post(BASE_URL).with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL).with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void corpoInvalidoRetorna400() throws Exception {
        mockMvc.perform(post(BASE_URL).with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void empresaInexistenteRetorna404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999999").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void enumsExpoemRotulos() throws Exception {
        mockMvc.perform(get(BASE_URL + "/enums").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipos[*].rotulo", hasItem("Clínica")))
                .andExpect(jsonPath("$.especialidades[*].rotulo", hasItem("Nutrição")));
    }

    @Test
    void profissionalNaoAcessaNenhumEndpoint() throws Exception {
        mockMvc.perform(get(BASE_URL).with(user("prof").roles("PROFISSIONAL"))).andExpect(status().isForbidden());
        mockMvc.perform(get(BASE_URL + "/enums").with(user("prof").roles("PROFISSIONAL"))).andExpect(status().isForbidden());
        mockMvc.perform(get(BASE_URL + "/1").with(user("prof").roles("PROFISSIONAL"))).andExpect(status().isForbidden());
        mockMvc.perform(post(BASE_URL).with(user("prof").roles("PROFISSIONAL"))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(BASE_URL + "/1").with(user("prof").roles("PROFISSIONAL"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"x\"}"))
                .andExpect(status().isForbidden());
        org.junit.jupiter.api.Assertions.assertTrue(organizacaoRepository.buscar(NOME).isEmpty());
    }

    @Test
    void anonimoRecebe401() throws Exception {
        mockMvc.perform(get(BASE_URL)).andExpect(status().isUnauthorized());
    }
}
