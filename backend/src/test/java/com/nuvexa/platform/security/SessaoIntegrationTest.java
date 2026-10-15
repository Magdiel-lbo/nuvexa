package com.nuvexa.platform.security;

import com.jayway.jsonpath.JsonPath;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.repository.UsuarioRepository;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sessões com access token curto + refresh rotativo, com JWT, banco e filtros reais.
 * IP próprio para não poluir o limite de tokens inválidos do 127.0.0.1 compartilhado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SessaoIntegrationTest {

    private static final String EMAIL = "sessao.usuario@nuvexa.test";
    private static final String SENHA = "senha12345";
    private static final String IP = "10.250.2.1";
    private static final String ENDPOINT_AUTENTICADO = "/api/v1/contexto";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void cadastrarUsuario() throws Exception {
        limpar();
        mockMvc.perform(post("/api/v1/auth/cadastrar").with(ip()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sessao Teste\",\"email\":\"" + EMAIL + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void limpar() {
        jdbc.update("DELETE FROM eventos_autenticacao WHERE ip = ?", IP);
        jdbc.queryForList("SELECT id FROM usuarios WHERE email = ?", Long.class, EMAIL).forEach(usuarioId -> {
            List<Long> organizacoes = jdbc.queryForList("SELECT organizacao_id FROM vinculos WHERE usuario_id = ?", Long.class, usuarioId);
            LimpezaDeUsuarios.removerEventosDe(jdbc, usuarioId);
            jdbc.update("DELETE FROM vinculos WHERE usuario_id = ?", usuarioId);
            organizacoes.forEach(organizacaoId -> jdbc.update("DELETE FROM organizacoes WHERE id = ?", organizacaoId));
            jdbc.update("DELETE FROM usuarios WHERE id = ?", usuarioId);
        });
    }

    @Test
    void loginDevolveAccessTokenCurtoERefreshToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").with(ip()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiraEmSegundos").value(900));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM sessoes s JOIN usuarios u ON u.id = s.usuario_id WHERE u.email = ?", Long.class, EMAIL))
                .isEqualTo(2L);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM sessoes WHERE hash_refresh_token NOT SIMILAR TO '[0-9a-f]{64}'", Long.class))
                .as("só hash SHA-256 no banco, nunca o token em claro").isZero();
    }

    @Test
    void refreshRotacionaORefreshEOAccessTokenAnteriorMorreNaHora() throws Exception {
        Par primeiro = login();

        Par segundo = par(renovar(primeiro.refresh).andExpect(status().isOk()));

        assertThat(segundo.refresh).isNotEqualTo(primeiro.refresh);
        usar(segundo.access).andExpect(status().isOk());
        usar(primeiro.access).andExpect(status().isUnauthorized());
    }

    @Test
    void reutilizarRefreshJaRotacionadoForaDaToleranciaRevogaAFamiliaInteira() throws Exception {
        Par primeiro = login();
        Par segundo = par(renovar(primeiro.refresh).andExpect(status().isOk()));
        jdbc.update("UPDATE sessoes SET rotacionada_em = now() - interval '1 minute' WHERE rotacionada_em IS NOT NULL");

        renovar(primeiro.refresh).andExpect(status().isUnauthorized());

        usar(segundo.access).andExpect(status().isUnauthorized());
        renovar(segundo.refresh).andExpect(status().isUnauthorized());
        assertThat(contar("REFRESH_REUTILIZADO")).isEqualTo(1);
    }

    @Test
    void corridaDeDuasAbasDentroDaToleranciaRecusaOSegundoSemDerrubarAFamilia() throws Exception {
        Par primeiro = login();
        Par segundo = par(renovar(primeiro.refresh).andExpect(status().isOk()));

        renovar(primeiro.refresh).andExpect(status().isUnauthorized());

        usar(segundo.access).andExpect(status().isOk());
        renovar(segundo.refresh).andExpect(status().isOk());
        assertThat(contar("REFRESH_REUTILIZADO")).isZero();
    }

    @Test
    void logoutRevogaOLoginInteiroEExigeSessao() throws Exception {
        Par par = login();

        mockMvc.perform(post("/api/v1/auth/logout")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + par.access)).andExpect(status().isNoContent());

        usar(par.access).andExpect(status().isUnauthorized());
        renovar(par.refresh).andExpect(status().isUnauthorized());
        assertThat(contar("LOGOUT")).isEqualTo(1);
    }

    @Test
    void logoutDeTodosOsDispositivosRevogaTodasAsSessoesDoUsuario() throws Exception {
        Par aparelho1 = login();
        Par aparelho2 = login();

        mockMvc.perform(post("/api/v1/auth/logout-todos").header("Authorization", "Bearer " + aparelho1.access))
                .andExpect(status().isNoContent());

        usar(aparelho1.access).andExpect(status().isUnauthorized());
        usar(aparelho2.access).andExpect(status().isUnauthorized());
        renovar(aparelho2.refresh).andExpect(status().isUnauthorized());
    }

    @Test
    void redefinirSenhaRevogaTodasAsSessoesDoUsuario() throws Exception {
        Par par = login();
        jdbc.update("UPDATE usuarios SET hash_token_redefinicao = ?, token_redefinicao_expira_em = now() + interval '10 minutes' WHERE email = ?",
                TokenOpaco.hash("token-sessao-teste"), EMAIL);

        mockMvc.perform(post("/api/v1/auth/redefinir-senha").with(ip()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token-sessao-teste\",\"novaSenha\":\"nova-senha-12345\"}"))
                .andExpect(status().isOk());

        usar(par.access).andExpect(status().isUnauthorized());
        renovar(par.refresh).andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioDesativadoNaoRenovaERevogaAFamilia() throws Exception {
        Par par = login();
        Usuario usuario = usuarioRepository.findByEmail(EMAIL).orElseThrow();
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        renovar(par.refresh).andExpect(status().isUnauthorized());

        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
        usar(par.access).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshExpiradoOuInexistenteRetorna401() throws Exception {
        Par par = login();
        jdbc.update("UPDATE sessoes SET expira_em = now() - interval '1 minute' WHERE usuario_id = (SELECT id FROM usuarios WHERE email = ?)", EMAIL);

        renovar(par.refresh).andExpect(status().isUnauthorized());
        renovar("refresh-que-nao-existe").andExpect(status().isUnauthorized());
        usar(par.access).andExpect(status().isUnauthorized());
    }

    @Test
    void accessTokenSemSessaoOuDeSessaoDeOutroUsuarioNaoAutentica() throws Exception {
        Usuario usuario = usuarioRepository.findByEmail(EMAIL).orElseThrow();

        usar(jwtService.generateToken(usuario, null)).andExpect(status().isUnauthorized());
        usar(jwtService.generateToken(usuario, 999999999L)).andExpect(status().isUnauthorized());
    }

    @Test
    void renovarExigeCorpoValido() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh").with(ip()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(ENDPOINT_AUTENTICADO)).andExpect(status().isUnauthorized());
    }

    // ---------- Auxiliares ----------

    private record Par(String access, String refresh) {
    }

    private Par login() throws Exception {
        String corpo = mockMvc.perform(post("/api/v1/auth/login").with(ip()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return new Par(JsonPath.read(corpo, "$.token"), JsonPath.read(corpo, "$.refreshToken"));
    }

    private ResultActions renovar(String refresh) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/refresh").with(ip())
                .contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + refresh + "\"}"));
    }

    private Par par(ResultActions resposta) throws Exception {
        String corpo = resposta.andReturn().getResponse().getContentAsString();
        return new Par(JsonPath.read(corpo, "$.token"), JsonPath.read(corpo, "$.refreshToken"));
    }

    private ResultActions usar(String access) throws Exception {
        return mockMvc.perform(get(ENDPOINT_AUTENTICADO).header("Authorization", "Bearer " + access));
    }

    private long contar(String tipo) {
        return jdbc.queryForObject("SELECT count(*) FROM eventos_autenticacao WHERE tipo_evento = ? AND email_informado = ?", Long.class, tipo, EMAIL);
    }

    private static RequestPostProcessor ip() {
        return request -> {
            request.setRemoteAddr(IP);
            return request;
        };
    }
}
