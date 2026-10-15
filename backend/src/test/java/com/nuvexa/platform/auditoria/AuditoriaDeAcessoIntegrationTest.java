package com.nuvexa.platform.auditoria;

import com.jayway.jsonpath.JsonPath;
import com.nuvexa.platform.security.TokenOpaco;
import com.nuvexa.testsupport.LimpezaDeUsuarios;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Auditoria de acesso a dado clínico e de eventos de autenticação, com JWT, banco e storage
 * reais (o download de anexo usa o MinIO do docker compose).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuditoriaDeAcessoIntegrationTest {

    private static final String DONO = "auditoria.dono@nuvexa.test";
    private static final String OUTRO = "auditoria.outro@nuvexa.test";
    private static final String SENHA = "senha12345";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;

    private String authDono;
    private String authOutro;
    private long usuarioDono;
    private long organizacaoDono;
    private long pacienteId;
    private long prontuarioId;
    private long avaliacaoId;
    private long planoId;
    private long anexoId;

    @BeforeAll
    void criarDados() throws Exception {
        limpar();
        authDono = cadastrar(DONO);
        authOutro = cadastrar(OUTRO);
        usuarioDono = jdbc.queryForObject("SELECT id FROM usuarios WHERE email = ?", Long.class, DONO);
        organizacaoDono = jdbc.queryForObject("SELECT organizacao_id FROM vinculos WHERE usuario_id = ?", Long.class, usuarioDono);

        pacienteId = criarId("/api/v1/pacientes", "{\"nome\":\"Paciente Auditoria\",\"dataNascimento\":\"1990-05-10\",\"sexo\":\"FEMININO\"}");
        mockMvc.perform(post("/api/v1/pacientes/" + pacienteId + "/perfil-nutricional").header("Authorization", authDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"altura\":1.70,\"pesoInicial\":68.0,\"objetivo\":\"EMAGRECIMENTO\",\"nivelAtividade\":\"SEDENTARIO\"}"))
                .andExpect(status().isCreated());
        prontuarioId = criarId("/api/v1/prontuarios", """
                {"pacienteId":%d,"autorId":%d,"secao":"EVOLUCAO","status":"RASCUNHO","conteudo":"evolucao","comAnexo":false}
                """.formatted(pacienteId, usuarioDono));
        avaliacaoId = criarId("/api/v1/avaliacoes", """
                {"pacienteId":%d,"avaliadorId":%d,"data":"%s","tipo":"ANTROPOMETRIA","status":"AGENDADA","peso":70.0}
                """.formatted(pacienteId, usuarioDono, LocalDate.now()));
        planoId = criarId("/api/v1/planos-alimentares", """
                {"pacienteId":%d,"autorId":%d,"nome":"Plano","dataInicio":"%s","calorias":2000,"refeicoesPorDia":5,"status":"RASCUNHO"}
                """.formatted(pacienteId, usuarioDono, LocalDate.now()));

        String anexo = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/prontuarios/" + prontuarioId + "/anexos")
                        .file(new MockMultipartFile("arquivo", "exame.pdf", "application/pdf", "%PDF-1.4 teste".getBytes()))
                        .header("Authorization", authDono))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        anexoId = ((Number) JsonPath.read(anexo, "$.id")).longValue();
    }

    @AfterAll
    void removerDados() throws Exception {
        if (anexoId != 0) {
            mockMvc.perform(delete("/api/v1/prontuarios/" + prontuarioId + "/anexos/" + anexoId).header("Authorization", authDono));
        }
        limpar();
    }

    @Test
    void leituraPorIdDeCadaRecursoClinicoRegistraQuemQuandoOrganizacaoEPaciente() throws Exception {
        long antes = contar("LEITURA");

        mockMvc.perform(get("/api/v1/prontuarios/" + prontuarioId).header("Authorization", authDono)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/avaliacoes/" + avaliacaoId).header("Authorization", authDono)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/planos-alimentares/" + planoId).header("Authorization", authDono)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/pacientes/" + pacienteId + "/perfil-nutricional").header("Authorization", authDono)).andExpect(status().isOk());

        assertThat(contar("LEITURA") - antes).isEqualTo(4);
        List<Map<String, Object>> eventos = jdbc.queryForList(
                "SELECT entidade_tipo, usuario_id, usuario_nome, organizacao_id, paciente_id FROM eventos_auditoria WHERE tipo_evento = 'LEITURA' AND organizacao_id = ?",
                organizacaoDono);
        assertThat(eventos).extracting(e -> e.get("entidade_tipo"))
                .containsExactlyInAnyOrder("PRONTUARIO", "AVALIACAO", "PLANO_ALIMENTAR", "PERFIL_NUTRICIONAL");
        assertThat(eventos).allSatisfy(evento -> {
            assertThat(((Number) evento.get("usuario_id")).longValue()).isEqualTo(usuarioDono);
            assertThat(evento.get("usuario_nome")).isEqualTo("Auditoria Teste");
            assertThat(((Number) evento.get("paciente_id")).longValue()).isEqualTo(pacienteId);
        });

        mockMvc.perform(get("/api/v1/prontuarios/" + prontuarioId + "/auditoria").header("Authorization", authDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipoEvento", hasItem("LEITURA")));
    }

    @Test
    void downloadDeAnexoRegistraOEventoComOPaciente() throws Exception {
        long antes = contar("DOWNLOAD_ANEXO");

        mockMvc.perform(get("/api/v1/prontuarios/" + prontuarioId + "/anexos/" + anexoId + "/download").header("Authorization", authDono))
                .andExpect(status().isOk());

        assertThat(contar("DOWNLOAD_ANEXO") - antes).isEqualTo(1);
        Map<String, Object> evento = jdbc.queryForMap(
                "SELECT paciente_id, dados_depois FROM eventos_auditoria WHERE tipo_evento = 'DOWNLOAD_ANEXO' AND organizacao_id = ? ORDER BY id DESC LIMIT 1",
                organizacaoDono);
        assertThat(((Number) evento.get("paciente_id")).longValue()).isEqualTo(pacienteId);
        assertThat((String) evento.get("dados_depois")).contains("exame.pdf");
    }

    @Test
    void acessoNegadoOuNaoEncontradoNaoGeraEventoDeLeitura() throws Exception {
        long antes = contar("LEITURA") + contar("DOWNLOAD_ANEXO");

        mockMvc.perform(get("/api/v1/prontuarios/" + prontuarioId).header("Authorization", authOutro)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/prontuarios/" + prontuarioId + "/anexos/" + anexoId + "/download").header("Authorization", authOutro))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/prontuarios/999999999").header("Authorization", authDono)).andExpect(status().isNotFound());

        assertThat(contar("LEITURA") + contar("DOWNLOAD_ANEXO")).isEqualTo(antes);
    }

    @Test
    void autenticacaoRegistraSucessoFalhaECadastroComIp() throws Exception {
        long sucessosAntes = contarAutenticacao("LOGIN_SUCESSO", DONO);
        long falhasAntes = contarAutenticacao("LOGIN_FALHA", DONO);

        login(DONO, SENHA).andExpect(status().isOk());
        login(DONO, "senha-errada").andExpect(status().isUnauthorized());
        login("auditoria.inexistente@nuvexa.test", "qualquer-senha").andExpect(status().isUnauthorized());

        assertThat(contarAutenticacao("LOGIN_SUCESSO", DONO) - sucessosAntes).isEqualTo(1);
        assertThat(contarAutenticacao("LOGIN_FALHA", DONO) - falhasAntes).isEqualTo(1);
        Map<String, Object> falhaDeEmailInexistente = jdbc.queryForMap(
                "SELECT usuario_id, ip FROM eventos_autenticacao WHERE tipo_evento = 'LOGIN_FALHA' AND email_informado = 'auditoria.inexistente@nuvexa.test'");
        assertThat(falhaDeEmailInexistente.get("usuario_id")).isNull();
        assertThat(falhaDeEmailInexistente.get("ip")).isNotNull();
        assertThat(contarAutenticacao("CADASTRO", DONO)).isEqualTo(1);

        jdbc.update("DELETE FROM eventos_autenticacao WHERE email_informado = 'auditoria.inexistente@nuvexa.test'");
    }

    @Test
    void esqueciSenhaDeEmailInexistenteERedefinicaoSaoRegistradas() throws Exception {
        String inexistente = "auditoria.reset.inexistente@nuvexa.test";
        mockMvc.perform(post("/api/v1/auth/esqueci-senha").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + inexistente + "\"}"))
                .andExpect(status().isOk());
        assertThat(contarAutenticacao("RESET_SOLICITADO", inexistente)).isEqualTo(1);

        jdbc.update("UPDATE usuarios SET hash_token_redefinicao = ?, token_redefinicao_expira_em = now() + interval '10 minutes' WHERE email = ?",
                TokenOpaco.hash("token-de-teste-auditoria"), OUTRO);
        mockMvc.perform(post("/api/v1/auth/redefinir-senha").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token-de-teste-auditoria\",\"novaSenha\":\"nova-senha-12345\"}"))
                .andExpect(status().isOk());
        assertThat(contarAutenticacao("SENHA_REDEFINIDA", OUTRO)).isEqualTo(1);

        jdbc.update("DELETE FROM eventos_autenticacao WHERE email_informado = ?", inexistente);
    }

    // ---------- Auxiliares ----------

    private org.springframework.test.web.servlet.ResultActions login(String email, String senha) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha)));
    }

    private long contar(String tipo) {
        return jdbc.queryForObject("SELECT count(*) FROM eventos_auditoria WHERE tipo_evento = ? AND organizacao_id = ?", Long.class, tipo, organizacaoDono);
    }

    private long contarAutenticacao(String tipo, String email) {
        return jdbc.queryForObject("SELECT count(*) FROM eventos_autenticacao WHERE tipo_evento = ? AND email_informado = ?", Long.class, tipo, email);
    }

    private long criarId(String url, String json) throws Exception {
        String resposta = mockMvc.perform(post(url).header("Authorization", authDono).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private String cadastrar(String email) throws Exception {
        String resposta = mockMvc.perform(post("/api/v1/auth/cadastrar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Auditoria Teste\",\"email\":\"" + email + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(resposta, "$.token");
    }

    private void limpar() {
        List<Long> organizacoes = new ArrayList<>(jdbc.queryForList(
                "SELECT v.organizacao_id FROM vinculos v JOIN usuarios u ON u.id = v.usuario_id WHERE u.email IN (?, ?)", Long.class, DONO, OUTRO));
        List<Long> usuarios = new ArrayList<>(jdbc.queryForList("SELECT id FROM usuarios WHERE email IN (?, ?)", Long.class, DONO, OUTRO));
        for (Long organizacaoId : organizacoes) {
            for (String tabela : List.of("eventos_auditoria", "prontuario_anexos", "prontuario_adendos", "avaliacoes", "planos_alimentares", "prontuarios")) {
                jdbc.update("DELETE FROM " + tabela + " WHERE organizacao_id = ?", organizacaoId);
            }
            jdbc.update("DELETE FROM perfis_nutricionais WHERE paciente_id IN (SELECT id FROM pacientes WHERE organizacao_id = ?)", organizacaoId);
            jdbc.update("DELETE FROM pacientes WHERE organizacao_id = ?", organizacaoId);
            jdbc.update("DELETE FROM vinculos WHERE organizacao_id = ?", organizacaoId);
            jdbc.update("DELETE FROM organizacoes WHERE id = ?", organizacaoId);
        }
        for (Long usuarioId : usuarios) {
            LimpezaDeUsuarios.removerEventosDe(jdbc, usuarioId);
            jdbc.update("DELETE FROM usuarios WHERE id = ?", usuarioId);
        }
    }
}
