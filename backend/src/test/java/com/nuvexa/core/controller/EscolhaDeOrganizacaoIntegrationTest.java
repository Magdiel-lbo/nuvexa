package com.nuvexa.core.controller;

import com.jayway.jsonpath.JsonPath;
import com.nuvexa.platform.email.EmailService;
import com.nuvexa.testsupport.LimpezaDeUsuarios;
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
import org.springframework.test.web.servlet.ResultActions;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Usuário com vínculos em duas organizações: a escolha por cabeçalho nunca concede acesso (só
 * escolhe entre vínculos ativos), e papel, dados e permissões seguem a organização escolhida.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EscolhaDeOrganizacaoIntegrationTest {

    private static final String DONO = "escolha.dono@nuvexa.test";
    private static final String VISITANTE = "escolha.visitante@nuvexa.test";
    private static final String ESTRANHO = "escolha.estranho@nuvexa.test";
    private static final String SENHA = "senha12345";
    private static final String CABECALHO = "X-Organizacao-Id";
    private static final String IP_DO_TESTE = "10.250.3.1";
    private static final Pattern TOKEN = Pattern.compile("token=([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private EmailService emailService;

    private String authDono;
    private String authVisitante;
    private String authEstranho;
    private long organizacaoDoDono;
    private long organizacaoDoVisitante;

    @BeforeAll
    void prepararOrganizacoes() throws Exception {
        limpar();
        authDono = cadastrar(DONO);
        authVisitante = cadastrar(VISITANTE);
        authEstranho = cadastrar(ESTRANHO);
        organizacaoDoDono = organizacaoPropriaDe(DONO);
        organizacaoDoVisitante = organizacaoPropriaDe(VISITANTE);

        mockMvc.perform(post("/api/v1/pacientes").header("Authorization", authDono).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Paciente da Org do Dono\",\"dataNascimento\":\"1990-01-01\",\"sexo\":\"FEMININO\"}"))
                .andExpect(status().isCreated());
    }

    @AfterAll
    void removerDadosCriados() {
        limpar();
    }

    @Test
    void convidarEAceitarComoUsuarioLogadoCriaOSegundoVinculoEOUsuarioPassaAEscolherEntreOrganizacoes() throws Exception {
        convidar(authDono, VISITANTE, "RECEPCAO").andExpect(status().isCreated());
        String token = tokenEnviadoPara(VISITANTE);

        aceitarLogado(authVisitante, token).andExpect(status().isCreated())
                .andExpect(jsonPath("$.organizacaoId").value(organizacaoDoDono))
                .andExpect(jsonPath("$.papel").value("RECEPCAO"));

        mockMvc.perform(get("/api/v1/contexto/organizacoes").header("Authorization", authVisitante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].organizacaoId", hasItem((int) organizacaoDoDono)))
                .andExpect(jsonPath("$[*].organizacaoId", hasItem((int) organizacaoDoVisitante)));

        // Sem cabeçalho: o vínculo mais antigo (a própria organização, onde é PROPRIETARIO).
        mockMvc.perform(get("/api/v1/contexto").header("Authorization", authVisitante))
                .andExpect(jsonPath("$.organizacaoId").value(organizacaoDoVisitante))
                .andExpect(jsonPath("$.papelOrganizacional").value("PROPRIETARIO"));
        mockMvc.perform(get("/api/v1/prontuarios").header("Authorization", authVisitante)).andExpect(status().isOk());

        // Com o cabeçalho: organização, papel, dados e permissões do dono.
        mockMvc.perform(get("/api/v1/contexto").header("Authorization", authVisitante).header(CABECALHO, organizacaoDoDono))
                .andExpect(jsonPath("$.organizacaoId").value(organizacaoDoDono))
                .andExpect(jsonPath("$.papelOrganizacional").value("RECEPCAO"));
        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", authVisitante).header(CABECALHO, organizacaoDoDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", hasItem("Paciente da Org do Dono")));
        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", authVisitante))
                .andExpect(jsonPath("$[*].nome", not(hasItem("Paciente da Org do Dono"))));
        mockMvc.perform(get("/api/v1/prontuarios").header("Authorization", authVisitante).header(CABECALHO, organizacaoDoDono))
                .andExpect(status().isForbidden());

        // Reaceitar o mesmo convite não é possível (uso único) e já é membro.
        aceitarLogado(authVisitante, token).andExpect(status().isBadRequest());
        convidar(authDono, VISITANTE, "MEMBRO").andExpect(status().isConflict());
    }

    @Test
    void cabecalhoNuncaConcedeAcessoAOrganizacaoSemVinculoAtivo() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", authEstranho).header(CABECALHO, organizacaoDoDono))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/contexto").header("Authorization", authEstranho).header(CABECALHO, 999999999L))
                .andExpect(status().isForbidden());
        // Cabeçalho malformado: 400 onde não há @PreAuthorize; nos endpoints protegidos o verificador nega (403).
        mockMvc.perform(get("/api/v1/contexto").header("Authorization", authEstranho).header(CABECALHO, "abc"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", authEstranho).header(CABECALHO, "abc"))
                .andExpect(status().isForbidden());
    }

    @Test
    void vinculoDesativadoNaOrganizacaoEscolhidaNegaOAcesso() throws Exception {
        String email = "escolha.desativado@nuvexa.test";
        String auth = cadastrar(email);
        convidar(authDono, email, "MEMBRO").andExpect(status().isCreated());
        aceitarLogado(auth, tokenEnviadoPara(email)).andExpect(status().isCreated());
        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", auth).header(CABECALHO, organizacaoDoDono)).andExpect(status().isOk());

        jdbc.update("UPDATE vinculos SET ativo = false WHERE organizacao_id = ? AND usuario_id = (SELECT id FROM usuarios WHERE email = ?)", organizacaoDoDono, email);

        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", auth).header(CABECALHO, organizacaoDoDono)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", auth)).andExpect(status().isOk());
    }

    @Test
    void conviteDeContaExistenteSoVaiPeloAceiteAutenticadoComOMesmoEmail() throws Exception {
        String email = "escolha.conta.existente@nuvexa.test";
        String auth = cadastrar(email);
        convidar(authDono, email, "MEMBRO").andExpect(status().isCreated());
        String token = tokenEnviadoPara(email);

        // Aceite público (cria conta) recusa: o e-mail já tem conta.
        mockMvc.perform(post("/api/v1/auth/convites/aceitar").with(ip()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"%s\",\"nome\":\"Alguém\",\"senha\":\"%s\"}".formatted(token, SENHA)))
                .andExpect(status().isConflict());
        // Outra pessoa logada não consegue usar o token de um convite que não é dela.
        aceitarLogado(authEstranho, token).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/convites/aceitar").contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isUnauthorized());
        // O titular consegue.
        aceitarLogado(auth, token).andExpect(status().isCreated());
    }

    @Test
    void conviteAceitoPorContaExistenteReativaVinculoInativoComOPapelDoConvite() throws Exception {
        String email = "escolha.reativa@nuvexa.test";
        String auth = cadastrar(email);
        convidar(authDono, email, "MEMBRO").andExpect(status().isCreated());
        aceitarLogado(auth, tokenEnviadoPara(email)).andExpect(status().isCreated());
        jdbc.update("UPDATE vinculos SET ativo = false WHERE organizacao_id = ? AND usuario_id = (SELECT id FROM usuarios WHERE email = ?)", organizacaoDoDono, email);

        convidar(authDono, email, "RECEPCAO").andExpect(status().isCreated());
        aceitarLogado(auth, ultimoTokenEnviadoPara(email)).andExpect(status().isCreated()).andExpect(jsonPath("$.papel").value("RECEPCAO"));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM vinculos WHERE organizacao_id = ? AND usuario_id = (SELECT id FROM usuarios WHERE email = ?)",
                Long.class, organizacaoDoDono, email)).isEqualTo(1L);
    }

    // ---------- Auxiliares ----------

    private ResultActions convidar(String authorization, String email, String papel) throws Exception {
        return mockMvc.perform(post("/api/v1/convites").header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"papel\":\"%s\"}".formatted(email, papel)));
    }

    private ResultActions aceitarLogado(String authorization, String token) throws Exception {
        return mockMvc.perform(post("/api/v1/convites/aceitar").with(ip()).header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"" + token + "\"}"));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor ip() {
        return request -> {
            request.setRemoteAddr(IP_DO_TESTE);
            return request;
        };
    }

    private String tokenEnviadoPara(String email) {
        return ultimoTokenEnviadoPara(email);
    }

    private String ultimoTokenEnviadoPara(String email) {
        ArgumentCaptor<String> corpo = ArgumentCaptor.forClass(String.class);
        verify(emailService, atLeastOnce()).enviar(eq(email), anyString(), corpo.capture());
        Matcher matcher = TOKEN.matcher(corpo.getValue());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private String cadastrar(String email) throws Exception {
        String resposta = mockMvc.perform(post("/api/v1/auth/cadastrar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Escolha Teste\",\"email\":\"" + email + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(resposta, "$.token");
    }

    private long organizacaoPropriaDe(String email) {
        return jdbc.queryForObject("SELECT v.organizacao_id FROM vinculos v JOIN usuarios u ON u.id = v.usuario_id WHERE u.email = ? ORDER BY v.id LIMIT 1", Long.class, email);
    }

    private void limpar() {
        jdbc.update("DELETE FROM eventos_autenticacao WHERE ip = ?", IP_DO_TESTE);
        List<Long> usuarios = new ArrayList<>(jdbc.queryForList("SELECT id FROM usuarios WHERE email LIKE 'escolha.%@nuvexa.test'", Long.class));
        List<Long> organizacoes = new ArrayList<>();
        for (Long usuarioId : usuarios) {
            organizacoes.addAll(jdbc.queryForList("SELECT organizacao_id FROM vinculos WHERE usuario_id = ? AND papel = 'PROPRIETARIO'", Long.class, usuarioId));
        }
        for (Long organizacaoId : organizacoes) {
            jdbc.update("DELETE FROM convites WHERE organizacao_id = ?", organizacaoId);
            for (String tabela : List.of("eventos_auditoria", "consultas", "avaliacoes", "prontuarios")) {
                jdbc.update("DELETE FROM " + tabela + " WHERE organizacao_id = ?", organizacaoId);
            }
            jdbc.update("DELETE FROM pacientes WHERE organizacao_id = ?", organizacaoId);
            jdbc.update("DELETE FROM vinculos WHERE organizacao_id = ?", organizacaoId);
            jdbc.update("DELETE FROM organizacoes WHERE id = ?", organizacaoId);
        }
        for (Long usuarioId : usuarios) {
            jdbc.update("DELETE FROM convites WHERE convidado_por_id = ?", usuarioId);
            jdbc.update("DELETE FROM vinculos WHERE usuario_id = ?", usuarioId);
            LimpezaDeUsuarios.removerEventosDe(jdbc, usuarioId);
            jdbc.update("DELETE FROM usuarios WHERE id = ?", usuarioId);
        }
    }
}
