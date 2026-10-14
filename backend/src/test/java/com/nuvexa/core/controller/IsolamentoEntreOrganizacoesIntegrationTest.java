package com.nuvexa.core.controller;

import com.jayway.jsonpath.JsonPath;
import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.ProntuarioAnexo;
import com.nuvexa.core.repository.ProntuarioAnexoRepository;
import com.nuvexa.core.repository.ProntuarioRepository;
import com.nuvexa.core.repository.UsuarioRepository;
import com.nuvexa.core.repository.VinculoRepository;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Isolamento entre organizações no nível HTTP, com o contexto completo (Spring Security real,
 * JWT real, banco real, sem mocks): o usuário da organização B tenta ler, editar, excluir e criar
 * referenciando dados que pertencem à organização A, para cada recurso clínico. Regra esperada:
 * id de outra organização é sempre 404 (nunca 403, que confirmaria a existência) e listagens e
 * relatórios nunca contêm dado alheio. Os dados da organização A devem sair intactos.
 *
 * <p>Não cobre o download do arquivo em si (exige o storage S3/MinIO): o anexo é criado direto no
 * banco e o teste prova que B é barrado antes de qualquer acesso ao storage.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IsolamentoEntreOrganizacoesIntegrationTest {

    private static final String EMAIL_A = "isolamento.a@nuvexa.test";
    private static final String EMAIL_B = "isolamento.b@nuvexa.test";
    private static final String MARCADOR = "ZZIsolamentoMarcador";
    private static final String CONTEUDO_ORIGINAL = "conteudo clinico original da organizacao A";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private VinculoRepository vinculoRepository;
    @Autowired
    private ProntuarioRepository prontuarioRepository;
    @Autowired
    private ProntuarioAnexoRepository prontuarioAnexoRepository;
    @Autowired
    private JdbcTemplate jdbc;

    private String authA;
    private String authB;
    private long usuarioA;
    private long usuarioB;
    private long pacienteA;
    private long pacienteB;
    private long consultaA;
    private long prontuarioA;
    private long prontuarioAssinadoA;
    private long anexoA;
    private long avaliacaoA;
    private long planoA;

    @BeforeAll
    void criarDadosDasDuasOrganizacoes() throws Exception {
        limpar();

        authA = cadastrar(EMAIL_A);
        authB = cadastrar(EMAIL_B);
        usuarioA = usuarioRepository.findByEmail(EMAIL_A).orElseThrow().getId();
        usuarioB = usuarioRepository.findByEmail(EMAIL_B).orElseThrow().getId();

        pacienteA = criarId(authA, "/api/v1/pacientes",
                "{\"nome\":\"Paciente %s A\",\"dataNascimento\":\"1990-05-10\",\"sexo\":\"FEMININO\"}".formatted(MARCADOR));
        pacienteB = criarId(authB, "/api/v1/pacientes",
                "{\"nome\":\"Paciente Org B\",\"dataNascimento\":\"1991-06-11\",\"sexo\":\"MASCULINO\"}");

        mockMvc.perform(post("/api/v1/pacientes/" + pacienteA + "/perfil-nutricional")
                        .header("Authorization", authA).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"altura\":1.70,\"pesoInicial\":68.0,\"objetivo\":\"EMAGRECIMENTO\",\"nivelAtividade\":\"SEDENTARIO\"}"))
                .andExpect(status().isCreated());

        consultaA = criarId(authA, "/api/v1/consultas", """
                {"pacienteId":%d,"profissionalId":%d,"dataHora":"%s","duracaoMinutos":30,
                 "tipo":"RETORNO","status":"AGENDADA","observacoes":"obs original"}
                """.formatted(pacienteA, usuarioA, LocalDateTime.now().plusDays(3).withNano(0)));

        prontuarioA = criarId(authA, "/api/v1/prontuarios", """
                {"pacienteId":%d,"autorId":%d,"secao":"EVOLUCAO","status":"RASCUNHO","conteudo":"%s","comAnexo":false}
                """.formatted(pacienteA, usuarioA, CONTEUDO_ORIGINAL));

        prontuarioAssinadoA = criarId(authA, "/api/v1/prontuarios", """
                {"pacienteId":%d,"autorId":%d,"secao":"ANAMNESE","status":"RASCUNHO","conteudo":"anamnese assinada","comAnexo":false}
                """.formatted(pacienteA, usuarioA));
        mockMvc.perform(patch("/api/v1/prontuarios/" + prontuarioAssinadoA + "/assinar").header("Authorization", authA))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/prontuarios/" + prontuarioAssinadoA + "/adendos")
                        .header("Authorization", authA).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"autorId\":" + usuarioA + ",\"texto\":\"adendo original\"}"))
                .andExpect(status().isCreated());

        Prontuario prontuario = prontuarioRepository.findById(prontuarioA).orElseThrow();
        anexoA = prontuarioAnexoRepository.save(ProntuarioAnexo.builder()
                .organizacao(prontuario.getOrganizacao())
                .prontuario(prontuario)
                .nomeOriginal("exame.pdf")
                .chaveStorage(prontuario.getOrganizacao().getId() + "/prontuarios/" + prontuarioA + "/isolamento-teste.pdf")
                .tipoMime("application/pdf")
                .tamanho(10L)
                .criadoPor(usuarioRepository.findById(usuarioA).orElseThrow())
                .build()).getId();

        avaliacaoA = criarId(authA, "/api/v1/avaliacoes", """
                {"pacienteId":%d,"avaliadorId":%d,"data":"%s","tipo":"ANTROPOMETRIA","status":"AGENDADA","peso":70.0}
                """.formatted(pacienteA, usuarioA, LocalDate.now()));

        planoA = criarId(authA, "/api/v1/planos-alimentares", """
                {"pacienteId":%d,"autorId":%d,"nome":"Plano %s","dataInicio":"%s","calorias":2000,"refeicoesPorDia":5,"status":"RASCUNHO"}
                """.formatted(pacienteA, usuarioA, MARCADOR, LocalDate.now()));
    }

    @AfterAll
    void removerDadosCriados() {
        limpar();
    }

    // ---------- Controle positivo: o dado existe e a organização A o enxerga ----------

    @Test
    void organizacaoDonaEnxergaOsProprioDados() throws Exception {
        for (String url : List.of(
                "/api/v1/pacientes/" + pacienteA,
                "/api/v1/consultas/" + consultaA,
                "/api/v1/prontuarios/" + prontuarioA,
                "/api/v1/prontuarios/" + prontuarioA + "/auditoria",
                "/api/v1/prontuarios/" + prontuarioAssinadoA + "/adendos",
                "/api/v1/prontuarios/" + prontuarioA + "/anexos",
                "/api/v1/avaliacoes/" + avaliacaoA,
                "/api/v1/planos-alimentares/" + planoA,
                "/api/v1/pacientes/" + pacienteA + "/perfil-nutricional")) {
            mockMvc.perform(get(url).header("Authorization", authA)).andExpect(status().isOk());
        }
        assertThat(corpo(get("/api/v1/prontuarios/" + prontuarioA + "/anexos"), authA)).contains("exame.pdf");
    }

    // ---------- Leitura por id ----------

    @Test
    void organizacaoBNaoLePorIdNenhumRecursoDaOrganizacaoA() throws Exception {
        for (String url : List.of(
                "/api/v1/pacientes/" + pacienteA,
                "/api/v1/consultas/" + consultaA,
                "/api/v1/prontuarios/" + prontuarioA,
                "/api/v1/prontuarios/" + prontuarioA + "/auditoria",
                "/api/v1/prontuarios/" + prontuarioAssinadoA + "/adendos",
                "/api/v1/prontuarios/" + prontuarioA + "/anexos",
                "/api/v1/prontuarios/" + prontuarioA + "/anexos/" + anexoA + "/download",
                "/api/v1/avaliacoes/" + avaliacaoA,
                "/api/v1/planos-alimentares/" + planoA,
                "/api/v1/pacientes/" + pacienteA + "/perfil-nutricional")) {
            mockMvc.perform(get(url).header("Authorization", authB)).andExpect(status().isNotFound());
        }
    }

    // ---------- Edição e exclusão por id ----------

    @Test
    void organizacaoBNaoEditaNemExcluiRecursosDaOrganizacaoA() throws Exception {
        usar(put("/api/v1/pacientes/" + pacienteA),
                "{\"nome\":\"Invadido\",\"dataNascimento\":\"1990-05-10\",\"sexo\":\"FEMININO\"}").andExpect(status().isNotFound());

        usar(put("/api/v1/consultas/" + consultaA),
                "{\"profissionalId\":%d,\"dataHora\":\"%s\",\"duracaoMinutos\":30,\"tipo\":\"RETORNO\",\"status\":\"AGENDADA\",\"observacoes\":\"invadido\"}"
                        .formatted(usuarioB, LocalDateTime.now().plusDays(4).withNano(0))).andExpect(status().isNotFound());
        usar(delete("/api/v1/consultas/" + consultaA), null).andExpect(status().isNotFound());

        usar(put("/api/v1/prontuarios/" + prontuarioA),
                "{\"autorId\":%d,\"secao\":\"EVOLUCAO\",\"status\":\"RASCUNHO\",\"conteudo\":\"invadido\",\"comAnexo\":false}"
                        .formatted(usuarioB)).andExpect(status().isNotFound());
        usar(patch("/api/v1/prontuarios/" + prontuarioA + "/assinar"), null).andExpect(status().isNotFound());
        usar(delete("/api/v1/prontuarios/" + prontuarioA), null).andExpect(status().isNotFound());

        usar(post("/api/v1/prontuarios/" + prontuarioAssinadoA + "/adendos"),
                "{\"autorId\":" + usuarioB + ",\"texto\":\"adendo invasor\"}").andExpect(status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/prontuarios/" + prontuarioA + "/anexos")
                        .file(new MockMultipartFile("arquivo", "invasor.pdf", "application/pdf", "%PDF-1.4".getBytes()))
                        .header("Authorization", authB))
                .andExpect(status().isNotFound());
        usar(delete("/api/v1/prontuarios/" + prontuarioA + "/anexos/" + anexoA), null).andExpect(status().isNotFound());

        usar(put("/api/v1/avaliacoes/" + avaliacaoA),
                "{\"avaliadorId\":%d,\"data\":\"%s\",\"tipo\":\"ANTROPOMETRIA\",\"status\":\"AGENDADA\",\"peso\":99.0}"
                        .formatted(usuarioB, LocalDate.now())).andExpect(status().isNotFound());
        usar(delete("/api/v1/avaliacoes/" + avaliacaoA), null).andExpect(status().isNotFound());

        usar(put("/api/v1/planos-alimentares/" + planoA),
                "{\"autorId\":%d,\"nome\":\"invadido\",\"dataInicio\":\"%s\",\"calorias\":1,\"refeicoesPorDia\":1,\"status\":\"RASCUNHO\"}"
                        .formatted(usuarioB, LocalDate.now())).andExpect(status().isNotFound());
        usar(delete("/api/v1/planos-alimentares/" + planoA), null).andExpect(status().isNotFound());

        String perfil = "/api/v1/pacientes/" + pacienteA + "/perfil-nutricional";
        usar(post(perfil), "{\"altura\":1.50,\"pesoInicial\":50.0,\"objetivo\":\"EMAGRECIMENTO\",\"nivelAtividade\":\"SEDENTARIO\"}")
                .andExpect(status().isNotFound());
        usar(put(perfil), "{\"altura\":1.50,\"objetivo\":\"EMAGRECIMENTO\",\"nivelAtividade\":\"SEDENTARIO\"}").andExpect(status().isNotFound());
        usar(delete(perfil), null).andExpect(status().isNotFound());

        verificarQueOsDadosDaOrganizacaoAEstaoIntactos();
    }

    // ---------- Criação referenciando dados de outra organização ----------

    @Test
    void organizacaoBNaoCriaRecursosApontandoParaPacienteOuProfissionalDaOrganizacaoA() throws Exception {
        usar(post("/api/v1/consultas"), """
                {"pacienteId":%d,"profissionalId":%d,"dataHora":"%s","duracaoMinutos":30,"tipo":"RETORNO","status":"AGENDADA"}
                """.formatted(pacienteA, usuarioB, LocalDateTime.now().plusDays(5).withNano(0))).andExpect(status().isNotFound());
        usar(post("/api/v1/consultas"), """
                {"pacienteId":%d,"profissionalId":%d,"dataHora":"%s","duracaoMinutos":30,"tipo":"RETORNO","status":"AGENDADA"}
                """.formatted(pacienteB, usuarioA, LocalDateTime.now().plusDays(5).withNano(0))).andExpect(status().isBadRequest());

        usar(post("/api/v1/prontuarios"), """
                {"pacienteId":%d,"autorId":%d,"secao":"EVOLUCAO","status":"RASCUNHO","conteudo":"x","comAnexo":false}
                """.formatted(pacienteA, usuarioB)).andExpect(status().isNotFound());
        usar(post("/api/v1/prontuarios"), """
                {"pacienteId":%d,"autorId":%d,"secao":"EVOLUCAO","status":"RASCUNHO","conteudo":"x","comAnexo":false}
                """.formatted(pacienteB, usuarioA)).andExpect(status().isBadRequest());

        usar(post("/api/v1/avaliacoes"), """
                {"pacienteId":%d,"avaliadorId":%d,"data":"%s","tipo":"ANTROPOMETRIA","status":"AGENDADA","peso":70.0}
                """.formatted(pacienteA, usuarioB, LocalDate.now())).andExpect(status().isNotFound());
        usar(post("/api/v1/avaliacoes"), """
                {"pacienteId":%d,"avaliadorId":%d,"data":"%s","tipo":"ANTROPOMETRIA","status":"AGENDADA","peso":70.0}
                """.formatted(pacienteB, usuarioA, LocalDate.now())).andExpect(status().isBadRequest());

        usar(post("/api/v1/planos-alimentares"), """
                {"pacienteId":%d,"autorId":%d,"nome":"x","dataInicio":"%s","calorias":2000,"refeicoesPorDia":5,"status":"RASCUNHO"}
                """.formatted(pacienteA, usuarioB, LocalDate.now())).andExpect(status().isNotFound());
        usar(post("/api/v1/planos-alimentares"), """
                {"pacienteId":%d,"autorId":%d,"nome":"x","dataInicio":"%s","calorias":2000,"refeicoesPorDia":5,"status":"RASCUNHO"}
                """.formatted(pacienteB, usuarioA, LocalDate.now())).andExpect(status().isBadRequest());
    }

    // ---------- Listagens e relatórios ----------

    @Test
    void listagensEPaginasDeRelatorioNuncaContemDadosDeOutraOrganizacao() throws Exception {
        List<String> listagens = List.of(
                "/api/v1/pacientes",
                "/api/v1/consultas",
                "/api/v1/prontuarios",
                "/api/v1/avaliacoes",
                "/api/v1/planos-alimentares",
                "/api/v1/perfis-nutricionais",
                "/api/v1/consultas/relatorio",
                "/api/v1/prontuarios/relatorio",
                "/api/v1/avaliacoes/relatorio",
                "/api/v1/planos-alimentares/relatorio",
                "/api/v1/pacientes/relatorio");

        for (String url : listagens) {
            assertThat(corpo(get(url), authA)).as("A deve ver o próprio dado em " + url).contains(MARCADOR);
            assertThat(corpo(get(url), authB)).as("B não pode ver dado da organização A em " + url).doesNotContain(MARCADOR);
        }
    }

    @Test
    void filtrarListagemPeloIdDoPacienteDeOutraOrganizacaoNaoRetornaNada() throws Exception {
        for (String url : List.of("/api/v1/consultas", "/api/v1/prontuarios", "/api/v1/avaliacoes", "/api/v1/planos-alimentares")) {
            mockMvc.perform(get(url).param("pacienteId", String.valueOf(pacienteA)).header("Authorization", authB))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ---------- Auxiliares ----------

    private void verificarQueOsDadosDaOrganizacaoAEstaoIntactos() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes/" + pacienteA).header("Authorization", authA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Paciente %s A".formatted(MARCADOR)));
        mockMvc.perform(get("/api/v1/consultas/" + consultaA).header("Authorization", authA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.observacoes").value("obs original"));
        mockMvc.perform(get("/api/v1/prontuarios/" + prontuarioA).header("Authorization", authA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.conteudo").value(CONTEUDO_ORIGINAL))
                .andExpect(jsonPath("$.status").value("RASCUNHO"));
        mockMvc.perform(get("/api/v1/prontuarios/" + prontuarioAssinadoA + "/adendos").header("Authorization", authA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/v1/prontuarios/" + prontuarioA + "/anexos").header("Authorization", authA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/v1/avaliacoes/" + avaliacaoA).header("Authorization", authA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.peso").value(70.0));
        mockMvc.perform(get("/api/v1/planos-alimentares/" + planoA).header("Authorization", authA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.calorias").value(2000));
        mockMvc.perform(get("/api/v1/pacientes/" + pacienteA + "/perfil-nutricional").header("Authorization", authA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.altura").value(1.7));
    }

    private ResultActions usar(MockHttpServletRequestBuilder requisicao, String json) throws Exception {
        requisicao.header("Authorization", authB);
        if (json != null) {
            requisicao.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mockMvc.perform(requisicao);
    }

    private String corpo(MockHttpServletRequestBuilder requisicao, String authorization) throws Exception {
        return mockMvc.perform(requisicao.header("Authorization", authorization))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private long criarId(String authorization, String url, String json) throws Exception {
        String resposta = mockMvc.perform(post(url).header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private String cadastrar(String email) throws Exception {
        String resposta = mockMvc.perform(post("/api/v1/auth/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Isolamento Teste\",\"email\":\"" + email + "\",\"senha\":\"senha12345\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(resposta, "$.token");
    }

    /** Remove só o que este teste criou: as organizações dos dois usuários de teste e tudo escopado por elas. */
    private void limpar() {
        List<Long> ids = new ArrayList<>();
        for (String email : List.of(EMAIL_A, EMAIL_B)) {
            usuarioRepository.findByEmail(email).ifPresent(usuario -> ids.add(usuario.getId()));
        }
        if (ids.isEmpty()) {
            return;
        }
        List<Long> organizacoes = new ArrayList<>();
        for (Long usuarioId : ids) {
            vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(usuarioId)
                    .forEach(vinculo -> organizacoes.add(vinculo.getOrganizacao().getId()));
        }
        for (Long organizacaoId : organizacoes) {
            for (String tabela : List.of("eventos_auditoria", "prontuario_anexos", "prontuario_adendos", "consulta_status_historico",
                    "consultas", "prontuarios", "avaliacoes", "planos_alimentares", "paciente_profissionais")) {
                jdbc.update("DELETE FROM " + tabela + " WHERE organizacao_id = ?", organizacaoId);
            }
            jdbc.update("DELETE FROM perfis_nutricionais WHERE paciente_id IN (SELECT id FROM pacientes WHERE organizacao_id = ?)", organizacaoId);
            jdbc.update("DELETE FROM pacientes WHERE organizacao_id = ?", organizacaoId);
            jdbc.update("DELETE FROM vinculos WHERE organizacao_id = ?", organizacaoId);
            jdbc.update("DELETE FROM organizacoes WHERE id = ?", organizacaoId);
        }
        for (Long usuarioId : ids) {
            jdbc.update("DELETE FROM usuarios WHERE id = ?", usuarioId);
        }
    }
}
