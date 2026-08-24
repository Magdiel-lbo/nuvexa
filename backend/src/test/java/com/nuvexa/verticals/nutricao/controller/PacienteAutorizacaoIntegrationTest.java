package com.nuvexa.verticals.nutricao.controller;

import com.nuvexa.verticals.nutricao.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorização com o contexto completo: SecurityConfig real e @PreAuthorize ativo.
 * Cobre a regra pré-existente de que só ADMIN exclui paciente, mais a fronteira
 * público/autenticado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PacienteAutorizacaoIntegrationTest {

    private static final String BASE_URL = "/api/v1/pacientes";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PacienteService pacienteService;

    // 10. ADMIN continua conseguindo excluir, como antes desta fase.
    @Test
    void adminDeveConseguirExcluirPaciente() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(pacienteService).delete(1L);
    }

    // 10. PROFISSIONAL continua sem poder excluir.
    @Test
    void profissionalNaoDeveConseguirExcluirPaciente() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1").with(user("prof").roles("PROFISSIONAL")))
                .andExpect(status().isForbidden());

        verify(pacienteService, never()).delete(1L);
    }

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
