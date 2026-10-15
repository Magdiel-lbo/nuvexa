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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Empresa (criada pelo ADMIN com o primeiro PROPRIETARIO convidado), equipe, vínculo
 * paciente↔profissional e a máscara das observações de consulta para quem não vê o clínico.
 * Contexto completo: JWT, banco e @PreAuthorize reais; só o e-mail é mockado (captura de token).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmpresaEEquipeIntegrationTest {

    private static final String ADMIN = "gestao.admin@nuvexa.test";
    private static final String DONO = "gestao.dono@nuvexa.test";
    private static final String GESTOR = "gestao.gestor@nuvexa.test";
    private static final String MEMBRO = "gestao.membro@nuvexa.test";
    private static final String RECEPCAO = "gestao.recepcao@nuvexa.test";
    private static final String SENHA = "senha12345";
    private static final String NOME_EMPRESA = "Empresa Gestao Teste";
    private static final String IP_DO_TESTE = "10.250.4.1";
    private static final Pattern TOKEN = Pattern.compile("token=([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private EmailService emailService;

    private String authAdmin;
    private String authDono;
    private String authGestor;
    private String authMembro;
    private String authRecepcao;
    private long empresaId;

    @BeforeAll
    void criarEmpresaEEquipe() throws Exception {
        limpar();
        authAdmin = cadastrar(ADMIN);
        jdbc.update("UPDATE usuarios SET perfil = 'ADMIN' WHERE email = ?", ADMIN);

        String resposta = mockMvc.perform(post("/api/v1/organizacoes").header("Authorization", authAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"%s\",\"tipo\":\"CLINICA\",\"especialidade\":\"NUTRICAO\",\"emailProprietario\":\"%s\"}".formatted(NOME_EMPRESA, DONO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.proprietarioAtivo").value(false))
                .andReturn().getResponse().getContentAsString();
        empresaId = ((Number) JsonPath.read(resposta, "$.id")).longValue();

        authDono = aceitarComoNovaConta(DONO, "Dono da Empresa");
        authGestor = convidarEAceitar(authDono, GESTOR, "GESTOR");
        authMembro = convidarEAceitar(authDono, MEMBRO, "MEMBRO");
        authRecepcao = convidarEAceitar(authDono, RECEPCAO, "RECEPCAO");
    }

    @AfterAll
    void removerDadosCriados() {
        limpar();
    }

    // ---------- Empresa ----------

    @Test
    void adminCriaEmpresaConvidandoOPrimeiroProprietarioQueEntraNaEmpresa() throws Exception {
        assertThat(jdbc.queryForObject("SELECT papel FROM vinculos v JOIN usuarios u ON u.id = v.usuario_id WHERE u.email = ? AND v.organizacao_id = ?",
                String.class, DONO, empresaId)).isEqualTo("PROPRIETARIO");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM vinculos v JOIN usuarios u ON u.id = v.usuario_id WHERE u.email = ?", Long.class, DONO))
                .as("o dono convidado não ganha organização individual própria").isEqualTo(1L);

        mockMvc.perform(get("/api/v1/organizacoes/" + empresaId).header("Authorization", authAdmin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.proprietarioAtivo").value(true));
        mockMvc.perform(get("/api/v1/organizacoes").param("busca", NOME_EMPRESA).header("Authorization", authAdmin))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].id", hasItem((int) empresaId)));
    }

    @Test
    void adminReenviaConviteDeProprietarioMasNaoParaQuemJaEMembro() throws Exception {
        String novo = "gestao.coproprietario@nuvexa.test";
        mockMvc.perform(post("/api/v1/organizacoes/" + empresaId + "/proprietario").header("Authorization", authAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + novo + "\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.papel").value("PROPRIETARIO"));
        mockMvc.perform(post("/api/v1/organizacoes/" + empresaId + "/proprietario").header("Authorization", authAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + DONO + "\"}"))
                .andExpect(status().isConflict());
        // Quem não é ADMIN não usa esse endpoint, nem sendo PROPRIETARIO.
        mockMvc.perform(post("/api/v1/organizacoes/" + empresaId + "/proprietario").header("Authorization", authDono)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + novo + "\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void soOProprietarioEditaOsDadosDaPropriaEmpresa() throws Exception {
        mockMvc.perform(get("/api/v1/minha-organizacao").header("Authorization", authDono))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(empresaId));
        mockMvc.perform(put("/api/v1/minha-organizacao").header("Authorization", authDono).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"%s\",\"razaoSocial\":\"Gestao Teste LTDA\"}".formatted(NOME_EMPRESA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.razaoSocial").value("Gestao Teste LTDA"));

        for (String auth : List.of(authGestor, authMembro, authRecepcao)) {
            mockMvc.perform(get("/api/v1/minha-organizacao").header("Authorization", auth)).andExpect(status().isForbidden());
            mockMvc.perform(put("/api/v1/minha-organizacao").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nome\":\"invadido\"}")).andExpect(status().isForbidden());
        }
    }

    // ---------- Equipe ----------

    @Test
    void gestorGerenciaMembrosAbaixoDeleMasNaoPromoveNemMexeEmProprietarioOuGestor() throws Exception {
        long vinculoMembro = vinculoDe(MEMBRO);

        mockMvc.perform(put("/api/v1/equipe/" + vinculoMembro + "/papel").header("Authorization", authGestor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"papel\":\"GESTOR\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/equipe/" + vinculoDe(DONO) + "/papel").header("Authorization", authGestor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"papel\":\"MEMBRO\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/equipe/" + vinculoDe(DONO)).header("Authorization", authGestor)).andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/equipe/" + vinculoMembro + "/papel").header("Authorization", authGestor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"papel\":\"RECEPCAO\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.papel").value("RECEPCAO"));
        mockMvc.perform(put("/api/v1/equipe/" + vinculoMembro + "/papel").header("Authorization", authGestor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"papel\":\"MEMBRO\"}"))
                .andExpect(status().isOk());

        for (String auth : List.of(authMembro, authRecepcao)) {
            mockMvc.perform(get("/api/v1/equipe").header("Authorization", auth)).andExpect(status().isForbidden());
        }
    }

    @Test
    void organizacaoNuncaFicaSemProprietarioAtivo() throws Exception {
        mockMvc.perform(put("/api/v1/equipe/" + vinculoDe(DONO) + "/papel").header("Authorization", authDono)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"papel\":\"GESTOR\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/v1/equipe/" + vinculoDe(DONO)).header("Authorization", authDono)).andExpect(status().isConflict());
    }

    @Test
    void removerMembroCortaOAcessoNaProximaRequisicaoSemApagarAConta() throws Exception {
        String email = "gestao.removido@nuvexa.test";
        String auth = convidarEAceitar(authDono, email, "MEMBRO");
        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", auth)).andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/equipe/" + vinculoDe(email)).header("Authorization", authGestor)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", auth)).andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM usuarios WHERE email = ?", Long.class, email)).isEqualTo(1L);
        mockMvc.perform(get("/api/v1/equipe").header("Authorization", authDono)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email=='" + email + "')].ativo").value(false));
    }

    // ---------- Paciente ↔ Profissional ----------

    @Test
    void vincularListarEDesvincularProfissionalDoPaciente() throws Exception {
        long pacienteId = criarPaciente(authDono, "Paciente do Vinculo");
        long profissional = usuarioId(MEMBRO);

        mockMvc.perform(post("/api/v1/pacientes/" + pacienteId + "/profissionais").header("Authorization", authRecepcao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"profissionalId\":" + profissional + "}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.ativo").value(true));
        mockMvc.perform(post("/api/v1/pacientes/" + pacienteId + "/profissionais").header("Authorization", authDono)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"profissionalId\":" + profissional + "}"))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/v1/pacientes/" + pacienteId + "/profissionais").header("Authorization", authDono))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].profissionalId").value(profissional));

        mockMvc.perform(delete("/api/v1/pacientes/" + pacienteId + "/profissionais/" + profissional).header("Authorization", authDono))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/pacientes/" + pacienteId + "/profissionais").header("Authorization", authDono))
                .andExpect(jsonPath("$[0].ativo").value(false));
        mockMvc.perform(post("/api/v1/pacientes/" + pacienteId + "/profissionais").header("Authorization", authDono)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"profissionalId\":" + profissional + "}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.ativo").value(true));

        mockMvc.perform(delete("/api/v1/pacientes/" + pacienteId + "/profissionais/" + usuarioId(ADMIN)).header("Authorization", authDono))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/pacientes/" + pacienteId + "/profissionais").header("Authorization", authDono)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"profissionalId\":" + usuarioId(ADMIN) + "}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/pacientes/" + pacienteId + "/profissionais").header("Authorization", authAdmin))
                .andExpect(status().isNotFound());
    }

    // ---------- Recepção e observações da consulta ----------

    @Test
    void recepcaoNaoVeNemApagaAsObservacoesClinicasDaConsulta() throws Exception {
        long pacienteId = criarPaciente(authDono, "Paciente da Consulta");
        long profissional = usuarioId(MEMBRO);
        String dataHora = LocalDateTime.now().plusDays(10).withNano(0).toString();

        String criada = mockMvc.perform(post("/api/v1/consultas").header("Authorization", authMembro).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":%d,"profissionalId":%d,"dataHora":"%s","duracaoMinutos":30,"tipo":"RETORNO",
                                 "status":"AGENDADA","observacoes":"anotacao clinica sigilosa","motivo":"retorno mensal"}
                                """.formatted(pacienteId, profissional, dataHora)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.observacoes").value("anotacao clinica sigilosa"))
                .andReturn().getResponse().getContentAsString();
        long consultaId = ((Number) JsonPath.read(criada, "$.id")).longValue();

        mockMvc.perform(get("/api/v1/consultas/" + consultaId).header("Authorization", authRecepcao))
                .andExpect(status().isOk()).andExpect(jsonPath("$.observacoes").doesNotExist())
                .andExpect(jsonPath("$.motivo").value("retorno mensal"));
        String listagemDaRecepcao = mockMvc.perform(get("/api/v1/consultas").header("Authorization", authRecepcao))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(listagemDaRecepcao).contains("retorno mensal").doesNotContain("anotacao clinica sigilosa");

        // A recepção remarca (o formulário dela não envia observações): o texto clínico fica intacto.
        mockMvc.perform(put("/api/v1/consultas/" + consultaId).header("Authorization", authRecepcao).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"profissionalId":%d,"dataHora":"%s","duracaoMinutos":45,"tipo":"RETORNO","status":"CONFIRMADA","motivo":"retorno mensal"}
                                """.formatted(profissional, dataHora)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.duracaoMinutos").value(45)).andExpect(jsonPath("$.observacoes").doesNotExist());
        mockMvc.perform(get("/api/v1/consultas/" + consultaId).header("Authorization", authMembro))
                .andExpect(jsonPath("$.observacoes").value("anotacao clinica sigilosa")).andExpect(jsonPath("$.status").value("CONFIRMADA"));

        // Observações enviadas pela recepção na criação também são descartadas.
        String daRecepcao = mockMvc.perform(post("/api/v1/consultas").header("Authorization", authRecepcao).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":%d,"profissionalId":%d,"dataHora":"%s","duracaoMinutos":30,"tipo":"RETORNO",
                                 "status":"AGENDADA","observacoes":"nao deveria gravar"}
                                """.formatted(pacienteId, profissional, LocalDateTime.now().plusDays(11).withNano(0))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long idDaRecepcao = ((Number) JsonPath.read(daRecepcao, "$.id")).longValue();
        assertThat(jdbc.queryForObject("SELECT observacoes FROM consultas WHERE id = ?", String.class, idDaRecepcao)).isNull();
    }

    // ---------- Auxiliares ----------

    private String convidarEAceitar(String autorizacaoDoConvidador, String email, String papel) throws Exception {
        mockMvc.perform(post("/api/v1/convites").header("Authorization", autorizacaoDoConvidador).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"papel\":\"%s\"}".formatted(email, papel)))
                .andExpect(status().isCreated());
        return aceitarComoNovaConta(email, "Convidado " + papel);
    }

    private String aceitarComoNovaConta(String email, String nome) throws Exception {
        ArgumentCaptor<String> corpo = ArgumentCaptor.forClass(String.class);
        verify(emailService, atLeastOnce()).enviar(eq(email), anyString(), corpo.capture());
        Matcher matcher = TOKEN.matcher(corpo.getValue());
        assertThat(matcher.find()).isTrue();
        String resposta = mockMvc.perform(post("/api/v1/auth/convites/aceitar").with(ip()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"%s\",\"nome\":\"%s\",\"senha\":\"%s\"}".formatted(matcher.group(1), nome, SENHA)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(resposta, "$.token");
    }

    private String cadastrar(String email) throws Exception {
        String resposta = mockMvc.perform(post("/api/v1/auth/cadastrar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Gestao Teste\",\"email\":\"" + email + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(resposta, "$.token");
    }

    private long criarPaciente(String autorizacao, String nome) throws Exception {
        String resposta = mockMvc.perform(post("/api/v1/pacientes").header("Authorization", autorizacao).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"dataNascimento\":\"1990-01-01\",\"sexo\":\"FEMININO\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private long usuarioId(String email) {
        return jdbc.queryForObject("SELECT id FROM usuarios WHERE email = ?", Long.class, email);
    }

    private long vinculoDe(String email) {
        return jdbc.queryForObject("SELECT v.id FROM vinculos v JOIN usuarios u ON u.id = v.usuario_id WHERE u.email = ? AND v.organizacao_id = ?",
                Long.class, email, empresaId);
    }

    private static RequestPostProcessor ip() {
        return request -> {
            request.setRemoteAddr(IP_DO_TESTE);
            return request;
        };
    }

    private void limpar() {
        jdbc.update("DELETE FROM eventos_autenticacao WHERE ip = ?", IP_DO_TESTE);
        List<Long> usuarios = new ArrayList<>(jdbc.queryForList("SELECT id FROM usuarios WHERE email LIKE 'gestao.%@nuvexa.test'", Long.class));
        List<Long> organizacoes = new ArrayList<>(jdbc.queryForList("SELECT id FROM organizacoes WHERE nome LIKE 'Empresa Gestao Teste%'", Long.class));
        for (Long usuarioId : usuarios) {
            for (Long organizacaoId : jdbc.queryForList("SELECT organizacao_id FROM vinculos WHERE usuario_id = ?", Long.class, usuarioId)) {
                if (!organizacoes.contains(organizacaoId)) {
                    organizacoes.add(organizacaoId);
                }
            }
        }
        for (Long organizacaoId : organizacoes) {
            jdbc.update("DELETE FROM convites WHERE organizacao_id = ?", organizacaoId);
            for (String tabela : List.of("eventos_auditoria", "consulta_status_historico", "consultas", "avaliacoes", "prontuarios", "paciente_profissionais")) {
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
