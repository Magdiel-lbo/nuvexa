package com.nuvexa.platform.security;

import com.nuvexa.testsupport.LimpezaDeUsuarios;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Limite de tentativas dos endpoints públicos, com contexto, banco e filtros reais. Cada cenário usa
 * IPs próprios (setRemoteAddr) para não poluir o 127.0.0.1 compartilhado pelos demais testes.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LimiteDeTentativasIntegrationTest {

    private static final String IP_A = "10.250.0.1";
    private static final String IP_B = "10.250.0.2";
    private static final String IP_C = "10.250.0.3";
    private static final String IP_D = "10.250.0.4";
    private static final List<String> IPS = List.of(IP_A, IP_B, IP_C, IP_D);
    private static final String EMAIL = "limite.usuario@nuvexa.test";
    private static final String OUTRO_EMAIL = "limite.outro@nuvexa.test";
    private static final String SENHA = "senha12345";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void prepararUsuarios() throws Exception {
        limpar();
        cadastrar(EMAIL, IP_D);
        cadastrar(OUTRO_EMAIL, IP_D);
        limparEventos();
    }

    @AfterEach
    void limpar() {
        limparEventos();
        for (String email : List.of(EMAIL, OUTRO_EMAIL)) {
            jdbc.queryForList("SELECT id FROM usuarios WHERE email = ?", Long.class, email).forEach(usuarioId -> {
                List<Long> organizacoes = jdbc.queryForList("SELECT organizacao_id FROM vinculos WHERE usuario_id = ?", Long.class, usuarioId);
                LimpezaDeUsuarios.removerEventosDe(jdbc, usuarioId);
                jdbc.update("DELETE FROM vinculos WHERE usuario_id = ?", usuarioId);
                organizacoes.forEach(organizacaoId -> jdbc.update("DELETE FROM organizacoes WHERE id = ?", organizacaoId));
                jdbc.update("DELETE FROM usuarios WHERE id = ?", usuarioId);
            });
        }
    }

    @Test
    void aSextaFalhaDeLoginDoMesmoEmailRetorna429MesmoComASenhaCerta() throws Exception {
        for (int i = 0; i < 5; i++) {
            login(EMAIL, "senha-errada", IP_A).andExpect(status().isUnauthorized());
        }

        login(EMAIL, SENHA, IP_B).andExpect(status().isTooManyRequests());
        assertThat(contar("LOGIN_BLOQUEADO", EMAIL)).isEqualTo(1);

        login(OUTRO_EMAIL, SENHA, IP_B).andExpect(status().isOk());
    }

    @Test
    void bloqueioDeEmailExpiraQuandoAsFalhasSaemDaJanela() throws Exception {
        for (int i = 0; i < 5; i++) {
            login(EMAIL, "senha-errada", IP_A).andExpect(status().isUnauthorized());
        }
        login(EMAIL, SENHA, IP_B).andExpect(status().isTooManyRequests());

        jdbc.update("UPDATE eventos_autenticacao SET criado_em = now() - interval '20 minutes' WHERE tipo_evento = 'LOGIN_FALHA' AND email_informado = ?", EMAIL);

        login(EMAIL, SENHA, IP_B).andExpect(status().isOk());
    }

    @Test
    void falhasDeLoginDeUmMesmoIpEmEmailsDiferentesBloqueiamOIpMasNaoOutros() throws Exception {
        for (int i = 0; i < 20; i++) {
            login("limite.alvo" + i + "@nuvexa.test", "qualquer-senha", IP_C).andExpect(status().isUnauthorized());
        }

        login(OUTRO_EMAIL, SENHA, IP_C).andExpect(status().isTooManyRequests());
        login(OUTRO_EMAIL, SENHA, IP_B).andExpect(status().isOk());
    }

    @Test
    void pedidosDeRedefinicaoAcimaDoLimiteNaoEnviamEmailNemMudamAResposta() throws Exception {
        String inexistente = "limite.reset.inexistente@nuvexa.test";
        String primeira = null;
        for (int i = 0; i < 3; i++) {
            primeira = esqueciSenha(inexistente, IP_A).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        }
        String quarta = esqueciSenha(inexistente, IP_A).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(quarta).isEqualTo(primeira);
        assertThat(contar("RESET_SOLICITADO", inexistente)).isEqualTo(3);
        assertThat(contar("RESET_BLOQUEADO", inexistente)).isEqualTo(1);
    }

    @Test
    void tokensInvalidosEmExcessoBloqueiamOIpInclusiveParaOAceiteDeConvite() throws Exception {
        for (int i = 0; i < 10; i++) {
            redefinirSenha("token-invalido-" + i, IP_A).andExpect(status().isBadRequest());
        }

        redefinirSenha("outro-token", IP_A).andExpect(status().isTooManyRequests());
        mockMvc.perform(post("/api/v1/auth/convites/aceitar").with(ip(IP_A)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"qualquer\",\"nome\":\"Fulano\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isTooManyRequests());

        redefinirSenha("outro-token", IP_B).andExpect(status().isBadRequest());
    }

    // ---------- Auxiliares ----------

    private static org.springframework.test.web.servlet.request.RequestPostProcessor ip(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    private ResultActions login(String email, String senha, String ip) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login").with(ip(ip)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha)));
    }

    private ResultActions esqueciSenha(String email, String ip) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/esqueci-senha").with(ip(ip)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"));
    }

    private ResultActions redefinirSenha(String token, String ip) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/redefinir-senha").with(ip(ip)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\",\"novaSenha\":\"nova-senha-12345\"}"));
    }

    private void cadastrar(String email, String ip) throws Exception {
        mockMvc.perform(post("/api/v1/auth/cadastrar").with(ip(ip)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Limite Teste\",\"email\":\"" + email + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isCreated());
    }

    private long contar(String tipo, String email) {
        return jdbc.queryForObject("SELECT count(*) FROM eventos_autenticacao WHERE tipo_evento = ? AND email_informado = ?", Long.class, tipo, email);
    }

    private void limparEventos() {
        for (String ip : IPS) {
            jdbc.update("DELETE FROM eventos_autenticacao WHERE ip = ?", ip);
        }
        jdbc.update("DELETE FROM eventos_autenticacao WHERE email_informado LIKE 'limite.%@nuvexa.test'");
    }
}
