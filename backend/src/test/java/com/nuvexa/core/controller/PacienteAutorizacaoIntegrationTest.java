package com.nuvexa.core.controller;

import com.nuvexa.core.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorização com o contexto completo: SecurityConfig real. Cobre só a fronteira
 * público/autenticado — hoje não existe endpoint de exclusão em PacienteController (a exclusão
 * do perfil nutricional vive em PerfilNutricionalController) nem @PreAuthorize por papel em
 * nenhum controller do sistema, então os testes antigos de "ADMIN exclui, PROFISSIONAL não"
 * foram removidos por não corresponderem a nenhum comportamento real hoje.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PacienteAutorizacaoIntegrationTest {

    private static final String BASE_URL = "/api/v1/pacientes";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PacienteService pacienteService;

    @Test
    void requisicaoAnonimaDeveSerRecusada() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioAutenticadoDeveAlcancarListagem() throws Exception {
        mockMvc.perform(get(BASE_URL).with(user("prof").roles("PROFISSIONAL")))
                .andExpect(status().isOk());
    }
}
