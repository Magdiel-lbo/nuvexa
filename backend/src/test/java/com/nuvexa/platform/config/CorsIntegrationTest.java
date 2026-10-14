package com.nuvexa.platform.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Preflight CORS com o contexto completo (Spring Security + WebConfig reais). Prova a
 * configuração do servidor; o comportamento final do navegador ainda depende dele.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsIntegrationTest {

    private static final String ENDPOINT_PATCH = "/api/v1/prontuarios/1/assinar";

    @Autowired
    private MockMvc mockMvc;

    @Value("${app.cors.allowed-origin}")
    private String origemAutorizada;

    @Test
    void preflightDePatchDeveSerAceitoParaOrigemAutorizada() throws Exception {
        mockMvc.perform(options(ENDPOINT_PATCH)
                        .header("Origin", origemAutorizada)
                        .header("Access-Control-Request-Method", "PATCH")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", origemAutorizada))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("PATCH")));
    }

    @Test
    void preflightDeOrigemNaoAutorizadaDeveSerBloqueado() throws Exception {
        mockMvc.perform(options(ENDPOINT_PATCH)
                        .header("Origin", "http://origem-nao-autorizada.example")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void metodoNaoConfiguradoContinuaBloqueadoNoPreflight() throws Exception {
        mockMvc.perform(options(ENDPOINT_PATCH)
                        .header("Origin", origemAutorizada)
                        .header("Access-Control-Request-Method", "TRACE"))
                .andExpect(status().isForbidden());
    }
}
