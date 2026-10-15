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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
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
 * A segunda clínica (terapia), criada pelo ADMIN: usa o núcleo (pacientes, agenda, consultas,
 * prontuário) e fica de fora de tudo que é da vertical de nutrição.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClinicaDeTerapiaIntegrationTest {

    private static final String ADMIN = "terapia.admin@nuvexa.test";
    private static final String DONO = "terapia.dono@nuvexa.test";
    private static final String SENHA = "senha12345";
    private static final String NOME_EMPRESA = "Clinica Terapia Teste";
    private static final String IP_DO_TESTE = "10.250.5.1";
    private static final Pattern TOKEN = Pattern.compile("token=([^\"]+)\"");

    private static final List<String> ENDPOINTS_DE_NUTRICAO = List.of(
            "/api/v1/avaliacoes", "/api/v1/avaliacoes/relatorio", "/api/v1/planos-alimentares", "/api/v1/planos-alimentares/relatorio",
            "/api/v1/perfis-nutricionais", "/api/v1/pacientes/relatorio");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private EmailService emailService;

    private String authAdmin;
    private String authDono;

    @BeforeAll
    void criarClinica() throws Exception {
        limpar();
        authAdmin = cadastrar(ADMIN);
        jdbc.update("UPDATE usuarios SET perfil = 'ADMIN' WHERE email = ?", ADMIN);

        mockMvc.perform(post("/api/v1/organizacoes").header("Authorization", authAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"%s\",\"tipo\":\"CLINICA\",\"especialidade\":\"TERAPIA\",\"emailProprietario\":\"%s\"}".formatted(NOME_EMPRESA, DONO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.especialidade").value("TERAPIA"));

        ArgumentCaptor<String> corpo = ArgumentCaptor.forClass(String.class);
        verify(emailService, atLeastOnce()).enviar(eq(DONO), anyString(), corpo.capture());
        Matcher matcher = TOKEN.matcher(corpo.getValue());
        assertThat(matcher.find()).isTrue();
        String resposta = mockMvc.perform(post("/api/v1/auth/convites/aceitar").with(ip()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"%s\",\"nome\":\"Terapeuta\",\"senha\":\"%s\"}".formatted(matcher.group(1), SENHA)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        authDono = "Bearer " + JsonPath.read(resposta, "$.token");
    }

    @AfterAll
    void removerDadosCriados() {
        limpar();
    }

    @Test
    void adminVeATerapiaComoOpcaoDeEspecialidade() throws Exception {
        mockMvc.perform(get("/api/v1/organizacoes/enums").header("Authorization", authAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especialidades[*].valor", hasItem("TERAPIA")))
                .andExpect(jsonPath("$.especialidades[*].rotulo", hasItem("Terapia")));
    }

    @Test
    void contextoInformaAEspecialidadeDaClinica() throws Exception {
        mockMvc.perform(get("/api/v1/contexto").header("Authorization", authDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especialidade").value("TERAPIA"))
                .andExpect(jsonPath("$.papelOrganizacional").value("PROPRIETARIO"));
    }

    @Test
    void clinicaDeTerapiaUsaONucleoDeCadastroAgendaEProntuario() throws Exception {
        String paciente = mockMvc.perform(post("/api/v1/pacientes").header("Authorization", authDono).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Paciente de Terapia\",\"dataNascimento\":\"1988-02-03\",\"sexo\":\"MASCULINO\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long pacienteId = ((Number) JsonPath.read(paciente, "$.id")).longValue();
        long usuarioId = jdbc.queryForObject("SELECT id FROM usuarios WHERE email = ?", Long.class, DONO);

        mockMvc.perform(post("/api/v1/consultas").header("Authorization", authDono).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":%d,"profissionalId":%d,"dataHora":"%s","duracaoMinutos":50,"tipo":"PRIMEIRA_CONSULTA","status":"AGENDADA"}
                                """.formatted(pacienteId, usuarioId, LocalDateTime.now().plusDays(2).withNano(0))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/prontuarios").header("Authorization", authDono).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":%d,"autorId":%d,"secao":"ANAMNESE","status":"RASCUNHO","conteudo":"queixa inicial","comAnexo":false}
                                """.formatted(pacienteId, usuarioId)))
                .andExpect(status().isCreated());

        for (String url : List.of("/api/v1/pacientes", "/api/v1/consultas", "/api/v1/prontuarios", "/api/v1/prontuarios/relatorio", "/api/v1/consultas/relatorio")) {
            mockMvc.perform(get(url).header("Authorization", authDono)).andExpect(status().isOk());
        }
    }

    @Test
    void tudoQueEDaVerticalDeNutricaoFicaBloqueadoParaATerapia() throws Exception {
        for (String url : ENDPOINTS_DE_NUTRICAO) {
            mockMvc.perform(get(url).header("Authorization", authDono)).andExpect(status().isForbidden());
        }
        mockMvc.perform(get("/api/v1/pacientes/1/perfil-nutricional").header("Authorization", authDono)).andExpect(status().isForbidden());
    }

    @Test
    void menuDaTerapiaNaoTemItensDeNutricao() throws Exception {
        mockMvc.perform(get("/api/v1/menus").header("Authorization", authDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..id", hasItem("consultas")))
                .andExpect(jsonPath("$..id", hasItem("prontuarios")))
                .andExpect(jsonPath("$..id", hasItem("equipe")))
                .andExpect(jsonPath("$..id", not(hasItem("nutricao"))))
                .andExpect(jsonPath("$..id", not(hasItem("avaliacoes"))));
    }

    private String cadastrar(String email) throws Exception {
        String resposta = mockMvc.perform(post("/api/v1/auth/cadastrar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Terapia Teste\",\"email\":\"" + email + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(resposta, "$.token");
    }

    private static RequestPostProcessor ip() {
        return request -> {
            request.setRemoteAddr(IP_DO_TESTE);
            return request;
        };
    }

    private void limpar() {
        jdbc.update("DELETE FROM eventos_autenticacao WHERE ip = ?", IP_DO_TESTE);
        List<Long> usuarios = new ArrayList<>(jdbc.queryForList("SELECT id FROM usuarios WHERE email LIKE 'terapia.%@nuvexa.test'", Long.class));
        List<Long> organizacoes = new ArrayList<>(jdbc.queryForList("SELECT id FROM organizacoes WHERE nome LIKE 'Clinica Terapia Teste%'", Long.class));
        for (Long usuarioId : usuarios) {
            for (Long organizacaoId : jdbc.queryForList("SELECT organizacao_id FROM vinculos WHERE usuario_id = ?", Long.class, usuarioId)) {
                if (!organizacoes.contains(organizacaoId)) {
                    organizacoes.add(organizacaoId);
                }
            }
        }
        for (Long organizacaoId : organizacoes) {
            jdbc.update("DELETE FROM convites WHERE organizacao_id = ?", organizacaoId);
            for (String tabela : List.of("eventos_auditoria", "consulta_status_historico", "consultas", "prontuarios")) {
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
