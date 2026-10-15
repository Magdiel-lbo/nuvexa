package com.nuvexa.core.controller;

import com.jayway.jsonpath.JsonPath;
import com.nuvexa.platform.email.EmailService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Convite de ponta a ponta com JWT, banco e @PreAuthorize reais; só o envio de e-mail é mockado
 * (para capturar o token que iria no link). Os papéis GESTOR, MEMBRO e RECEPCAO dos testes nascem
 * pelo próprio fluxo de convite e aceite.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConviteIntegrationTest {

    private static final String DONO = "convite.dono@nuvexa.test";
    private static final String OUTRO_DONO = "convite.outro@nuvexa.test";
    private static final String GESTOR = "convite.gestor@nuvexa.test";
    private static final String MEMBRO = "convite.membro@nuvexa.test";
    private static final String RECEPCAO = "convite.recepcao@nuvexa.test";
    private static final String SENHA = "senha12345";
    private static final Pattern TOKEN = Pattern.compile("token=([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private EmailService emailService;

    private String authDono;
    private String authOutroDono;
    private String authGestor;
    private String authMembro;
    private String authRecepcao;

    @BeforeAll
    void prepararOrganizacoesEEquipe() throws Exception {
        limpar();
        authDono = cadastrar(DONO);
        authOutroDono = cadastrar(OUTRO_DONO);
        authGestor = convidarEAceitar(authDono, GESTOR, "GESTOR");
        authMembro = convidarEAceitar(authDono, MEMBRO, "MEMBRO");
        authRecepcao = convidarEAceitar(authDono, RECEPCAO, "RECEPCAO");
    }

    @AfterAll
    void removerDadosCriados() {
        limpar();
    }

    @Test
    void aceiteCriaContaNaOrganizacaoDeQuemConvidouComOPapelDoConviteESemOrganizacaoPropria() throws Exception {
        long organizacaoDoDono = organizacaoDe(DONO);

        assertThat(organizacaoDe(GESTOR)).isEqualTo(organizacaoDoDono);
        assertThat(papelDe(GESTOR)).isEqualTo("GESTOR");
        assertThat(papelDe(MEMBRO)).isEqualTo("MEMBRO");
        assertThat(papelDe(RECEPCAO)).isEqualTo("RECEPCAO");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM vinculos v JOIN usuarios u ON u.id = v.usuario_id WHERE u.email = ?", Long.class, GESTOR))
                .isEqualTo(1L);

        mockMvc.perform(get("/api/v1/contexto").header("Authorization", authGestor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papelOrganizacional").value("GESTOR"));
    }

    @Test
    void respostaEListagemNuncaExpoemOToken() throws Exception {
        String email = "convite.resposta@nuvexa.test";
        String resposta = convidar(authDono, email, "MEMBRO").andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.papel").value("MEMBRO"))
                .andReturn().getResponse().getContentAsString();
        String token = tokenEnviadoPara(email);

        assertThat(resposta).doesNotContain(token).doesNotContain("hash");
        String listagem = mockMvc.perform(get("/api/v1/convites").header("Authorization", authDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(email)))
                .andReturn().getResponse().getContentAsString();
        assertThat(listagem).doesNotContain(token).doesNotContain("hash");
    }

    @Test
    void tokenDeUsoUnicoTokenInvalidoEExpiradoSaoRecusados() throws Exception {
        String email = "convite.uso.unico@nuvexa.test";
        convidar(authDono, email, "MEMBRO").andExpect(status().isCreated());
        String token = tokenEnviadoPara(email);

        aceitar(token, "Primeira Vez").andExpect(status().isCreated());
        aceitar(token, "Segunda Vez").andExpect(status().isBadRequest());
        aceitar("token-que-nao-existe", "Qualquer").andExpect(status().isBadRequest());

        String emailExpirado = "convite.expirado@nuvexa.test";
        convidar(authDono, emailExpirado, "MEMBRO").andExpect(status().isCreated());
        String tokenExpirado = tokenEnviadoPara(emailExpirado);
        jdbc.update("UPDATE convites SET expira_em = now() - interval '1 minute' WHERE email = ?", emailExpirado);
        aceitar(tokenExpirado, "Tarde Demais").andExpect(status().isBadRequest());
        assertThat(usuarioExiste(emailExpirado)).isFalse();
    }

    @Test
    void novoConviteRevogaOPendenteAnteriorEORevogadoNaoPodeSerAceito() throws Exception {
        String email = "convite.reenvio@nuvexa.test";
        convidar(authDono, email, "MEMBRO").andExpect(status().isCreated());
        String tokenAntigo = tokenEnviadoPara(email);

        convidar(authDono, email, "RECEPCAO").andExpect(status().isCreated());
        String tokenNovo = ultimoTokenEnviadoPara(email);

        aceitar(tokenAntigo, "Antigo").andExpect(status().isBadRequest());
        aceitar(tokenNovo, "Novo").andExpect(status().isCreated());
        assertThat(papelDe(email)).isEqualTo("RECEPCAO");

        String emailRevogado = "convite.revogado@nuvexa.test";
        String resposta = convidar(authDono, emailRevogado, "MEMBRO").andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(resposta, "$.id")).longValue();
        String tokenRevogado = tokenEnviadoPara(emailRevogado);
        mockMvc.perform(delete("/api/v1/convites/" + id).header("Authorization", authDono)).andExpect(status().isNoContent());
        aceitar(tokenRevogado, "Revogado").andExpect(status().isBadRequest());
        assertThat(usuarioExiste(emailRevogado)).isFalse();
    }

    @Test
    void naoConvidaEmailQueJaTemContaNemRevogaConviteJaAceito() throws Exception {
        convidar(authDono, GESTOR, "MEMBRO").andExpect(status().isConflict());
        convidar(authDono, DONO, "MEMBRO").andExpect(status().isConflict());

        Long idAceito = jdbc.queryForObject("SELECT id FROM convites WHERE email = ?", Long.class, MEMBRO);
        mockMvc.perform(delete("/api/v1/convites/" + idAceito).header("Authorization", authDono)).andExpect(status().isBadRequest());
    }

    @Test
    void ninguemConcedePapelAcimaDoProprio() throws Exception {
        convidar(authGestor, "convite.g.proprietario@nuvexa.test", "PROPRIETARIO").andExpect(status().isForbidden());
        convidar(authGestor, "convite.g.gestor@nuvexa.test", "GESTOR").andExpect(status().isForbidden());
        convidar(authGestor, "convite.g.membro@nuvexa.test", "MEMBRO").andExpect(status().isCreated());
        convidar(authGestor, "convite.g.recepcao@nuvexa.test", "RECEPCAO").andExpect(status().isCreated());

        convidar(authDono, "convite.d.proprietario@nuvexa.test", "PROPRIETARIO").andExpect(status().isCreated());
        convidar(authDono, "convite.d.gestor@nuvexa.test", "GESTOR").andExpect(status().isCreated());

        String resposta = convidar(authDono, "convite.gestor.revoga@nuvexa.test", "GESTOR").andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long idDeGestor = ((Number) JsonPath.read(resposta, "$.id")).longValue();
        mockMvc.perform(delete("/api/v1/convites/" + idDeGestor).header("Authorization", authGestor)).andExpect(status().isForbidden());
    }

    @Test
    void membroERecepcaoNaoGerenciamConvites() throws Exception {
        for (String authorization : List.of(authMembro, authRecepcao)) {
            mockMvc.perform(get("/api/v1/convites").header("Authorization", authorization)).andExpect(status().isForbidden());
            convidar(authorization, "convite.negado@nuvexa.test", "MEMBRO").andExpect(status().isForbidden());
            mockMvc.perform(delete("/api/v1/convites/1").header("Authorization", authorization)).andExpect(status().isForbidden());
        }
        assertThat(usuarioExiste("convite.negado@nuvexa.test")).isFalse();
    }

    @Test
    void organizacaoNaoVeNemRevogaConvitesDeOutraOrganizacao() throws Exception {
        String email = "convite.isolamento@nuvexa.test";
        String resposta = convidar(authDono, email, "MEMBRO").andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(resposta, "$.id")).longValue();

        mockMvc.perform(get("/api/v1/convites").header("Authorization", authOutroDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", not(hasItem(email))));
        mockMvc.perform(delete("/api/v1/convites/" + id).header("Authorization", authOutroDono)).andExpect(status().isNotFound());
    }

    @Test
    void convidadoComPapelDeRecepcaoNaoAcessaOClinicoMasAcessaOCadastro() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", authRecepcao)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/prontuarios").header("Authorization", authRecepcao)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/prontuarios").header("Authorization", authMembro)).andExpect(status().isOk());
    }

    @Test
    void conviteExigeAutenticacaoMasOAceiteEPublico() throws Exception {
        mockMvc.perform(get("/api/v1/convites")).andExpect(status().isUnauthorized());
        aceitar("qualquer", "Sem Login").andExpect(status().isBadRequest());
    }

    // ---------- Auxiliares ----------

    private org.springframework.test.web.servlet.ResultActions convidar(String authorization, String email, String papel) throws Exception {
        return mockMvc.perform(post("/api/v1/convites").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"papel\":\"%s\"}".formatted(email, papel)));
    }

    private org.springframework.test.web.servlet.ResultActions aceitar(String token, String nome) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/convites/aceitar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"%s\",\"nome\":\"%s\",\"senha\":\"%s\"}".formatted(token, nome, SENHA)));
    }

    private String convidarEAceitar(String authorizationDoConvidador, String email, String papel) throws Exception {
        convidar(authorizationDoConvidador, email, papel).andExpect(status().isCreated());
        String resposta = aceitar(tokenEnviadoPara(email), "Convidado " + papel).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(resposta, "$.token");
    }

    private String tokenEnviadoPara(String email) {
        return ultimoTokenEnviadoPara(email);
    }

    private String ultimoTokenEnviadoPara(String email) {
        ArgumentCaptor<String> corpo = ArgumentCaptor.forClass(String.class);
        verify(emailService, atLeastOnce()).enviar(eq(email), anyString(), corpo.capture());
        Matcher matcher = TOKEN.matcher(corpo.getValue());
        assertThat(matcher.find()).as("link com token no e-mail para " + email).isTrue();
        return matcher.group(1);
    }

    private String cadastrar(String email) throws Exception {
        String resposta = mockMvc.perform(post("/api/v1/auth/cadastrar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Convite Teste\",\"email\":\"" + email + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(resposta, "$.token");
    }

    private long organizacaoDe(String email) {
        return jdbc.queryForObject("SELECT v.organizacao_id FROM vinculos v JOIN usuarios u ON u.id = v.usuario_id WHERE u.email = ?", Long.class, email);
    }

    private String papelDe(String email) {
        return jdbc.queryForObject("SELECT v.papel FROM vinculos v JOIN usuarios u ON u.id = v.usuario_id WHERE u.email = ?", String.class, email);
    }

    private boolean usuarioExiste(String email) {
        return jdbc.queryForObject("SELECT count(*) FROM usuarios WHERE email = ?", Long.class, email) > 0;
    }

    /** Remove só o que o teste criou: organizações dos dois donos, convites, convidados e tudo escopado por elas. */
    private void limpar() {
        List<Long> organizacoes = new ArrayList<>(jdbc.queryForList(
                "SELECT v.organizacao_id FROM vinculos v JOIN usuarios u ON u.id = v.usuario_id WHERE u.email IN (?, ?)", Long.class, DONO, OUTRO_DONO));
        List<Long> usuarios = new ArrayList<>();
        for (Long organizacaoId : organizacoes) {
            usuarios.addAll(jdbc.queryForList("SELECT usuario_id FROM vinculos WHERE organizacao_id = ?", Long.class, organizacaoId));
        }
        usuarios.addAll(jdbc.queryForList("SELECT id FROM usuarios WHERE email LIKE 'convite.%@nuvexa.test'", Long.class));
        for (Long organizacaoId : organizacoes) {
            jdbc.update("DELETE FROM convites WHERE organizacao_id = ?", organizacaoId);
            for (String tabela : List.of("eventos_auditoria", "consultas", "avaliacoes", "prontuarios", "pacientes")) {
                jdbc.update("DELETE FROM " + tabela + " WHERE organizacao_id = ?", organizacaoId);
            }
            jdbc.update("DELETE FROM vinculos WHERE organizacao_id = ?", organizacaoId);
            jdbc.update("DELETE FROM organizacoes WHERE id = ?", organizacaoId);
        }
        for (Long usuarioId : usuarios.stream().distinct().toList()) {
            jdbc.update("DELETE FROM convites WHERE convidado_por_id = ?", usuarioId);
            jdbc.update("DELETE FROM vinculos WHERE usuario_id = ?", usuarioId);
            jdbc.update("DELETE FROM usuarios WHERE id = ?", usuarioId);
        }
    }
}
