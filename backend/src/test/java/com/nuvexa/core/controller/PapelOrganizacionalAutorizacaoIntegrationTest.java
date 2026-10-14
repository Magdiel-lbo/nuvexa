package com.nuvexa.core.controller;

import com.jayway.jsonpath.JsonPath;
import com.nuvexa.core.model.PapelOrganizacional;
import com.nuvexa.core.model.Vinculo;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorização por papel organizacional com o contexto completo (JWT, banco e @PreAuthorize reais).
 * O papel é lido do banco a cada requisição, então o mesmo token é reutilizado e só o vínculo muda.
 *
 * <p>Regra: RECEPCAO cadastra pacientes e agenda consultas, mas não acessa nada clínico;
 * PROPRIETARIO, GESTOR e MEMBRO acessam os dois grupos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PapelOrganizacionalAutorizacaoIntegrationTest {

    private static final String EMAIL = "papel.autorizacao@nuvexa.test";

    private static final List<String> LEITURAS_CLINICAS = List.of(
            "/api/v1/prontuarios",
            "/api/v1/prontuarios/relatorio",
            "/api/v1/prontuarios/enums",
            "/api/v1/avaliacoes",
            "/api/v1/avaliacoes/relatorio",
            "/api/v1/planos-alimentares",
            "/api/v1/planos-alimentares/relatorio",
            "/api/v1/perfis-nutricionais",
            "/api/v1/pacientes/relatorio");

    private static final List<String> LEITURAS_DE_CADASTRO_E_AGENDA = List.of(
            "/api/v1/pacientes",
            "/api/v1/pacientes/enums",
            "/api/v1/consultas",
            "/api/v1/consultas/profissionais",
            "/api/v1/consultas/relatorio");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private VinculoRepository vinculoRepository;
    @Autowired
    private JdbcTemplate jdbc;

    private String authorization;
    private long usuarioId;

    @BeforeAll
    void cadastrarUsuario() throws Exception {
        limpar();
        String resposta = mockMvc.perform(post("/api/v1/auth/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Papel Teste\",\"email\":\"" + EMAIL + "\",\"senha\":\"senha12345\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        authorization = "Bearer " + JsonPath.read(resposta, "$.token");
        usuarioId = usuarioRepository.findByEmail(EMAIL).orElseThrow().getId();
    }

    @AfterAll
    void removerDadosCriados() {
        limpar();
    }

    @Test
    void papeisClinicosAcessamDadoClinicoEDeCadastro() throws Exception {
        for (PapelOrganizacional papel : List.of(PapelOrganizacional.PROPRIETARIO, PapelOrganizacional.GESTOR, PapelOrganizacional.MEMBRO)) {
            definirPapel(papel);
            for (String url : LEITURAS_CLINICAS) {
                mockMvc.perform(get(url).header("Authorization", authorization))
                        .andExpect(status().isOk());
            }
            for (String url : LEITURAS_DE_CADASTRO_E_AGENDA) {
                mockMvc.perform(get(url).header("Authorization", authorization))
                        .andExpect(status().isOk());
            }
        }
    }

    @Test
    void recepcaoAcessaCadastroEAgendaMasNaoOClinico() throws Exception {
        definirPapel(PapelOrganizacional.RECEPCAO);

        for (String url : LEITURAS_DE_CADASTRO_E_AGENDA) {
            mockMvc.perform(get(url).header("Authorization", authorization))
                    .andExpect(status().isOk());
        }
        for (String url : LEITURAS_CLINICAS) {
            mockMvc.perform(get(url).header("Authorization", authorization))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void recepcaoNaoExecutaAcoesClinicasDeEscritaNemAcessaPorId() throws Exception {
        definirPapel(PapelOrganizacional.RECEPCAO);

        mockMvc.perform(post("/api/v1/prontuarios").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":1,\"autorId\":1,\"secao\":\"EVOLUCAO\",\"status\":\"RASCUNHO\",\"conteudo\":\"x\",\"comAnexo\":false}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/prontuarios/1/assinar").header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/prontuarios/1/anexos/1/download").header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/prontuarios/1/adendos").header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/pacientes/1/perfil-nutricional").header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/avaliacoes").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":1,\"avaliadorId\":1,\"data\":\"2026-01-01\",\"tipo\":\"ANTROPOMETRIA\",\"status\":\"AGENDADA\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void recepcaoCadastraPacienteEAgendaConsulta() throws Exception {
        definirPapel(PapelOrganizacional.RECEPCAO);

        String paciente = mockMvc.perform(post("/api/v1/pacientes").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Paciente da Recepcao\",\"dataNascimento\":\"1992-03-04\",\"sexo\":\"FEMININO\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long pacienteId = ((Number) JsonPath.read(paciente, "$.id")).longValue();

        mockMvc.perform(post("/api/v1/consultas").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":%d,"profissionalId":%d,"dataHora":"%s","duracaoMinutos":30,"tipo":"PRIMEIRA_CONSULTA","status":"AGENDADA"}
                                """.formatted(pacienteId, usuarioId, LocalDateTime.now().plusDays(2).withNano(0))))
                .andExpect(status().isCreated());
    }

    @Test
    void papelEhLidoDoBancoEmCadaRequisicaoEUmaMudancaValeParaOMesmoToken() throws Exception {
        definirPapel(PapelOrganizacional.MEMBRO);
        mockMvc.perform(get("/api/v1/prontuarios").header("Authorization", authorization)).andExpect(status().isOk());

        definirPapel(PapelOrganizacional.RECEPCAO);
        mockMvc.perform(get("/api/v1/prontuarios").header("Authorization", authorization)).andExpect(status().isForbidden());

        definirPapel(PapelOrganizacional.GESTOR);
        mockMvc.perform(get("/api/v1/prontuarios").header("Authorization", authorization)).andExpect(status().isOk());
    }

    @Test
    void vinculoRevogadoNegaOAcessoAoClinicoEAoCadastro() throws Exception {
        Vinculo vinculo = vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(usuarioId).getFirst();
        vinculo.setPapel(PapelOrganizacional.PROPRIETARIO);
        vinculo.setAtivo(false);
        vinculoRepository.save(vinculo);
        try {
            mockMvc.perform(get("/api/v1/prontuarios").header("Authorization", authorization)).andExpect(status().isForbidden());
            mockMvc.perform(get("/api/v1/pacientes").header("Authorization", authorization)).andExpect(status().isForbidden());
        } finally {
            vinculo.setAtivo(true);
            vinculoRepository.save(vinculo);
        }
    }

    private void definirPapel(PapelOrganizacional papel) {
        Vinculo vinculo = vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(usuarioId).getFirst();
        vinculo.setPapel(papel);
        vinculoRepository.save(vinculo);
    }

    private void limpar() {
        usuarioRepository.findByEmail(EMAIL).ifPresent(usuario -> {
            List<Long> organizacoes = jdbc.queryForList("SELECT organizacao_id FROM vinculos WHERE usuario_id = ?", Long.class, usuario.getId());
            for (Long organizacaoId : organizacoes) {
                for (String tabela : List.of("eventos_auditoria", "consulta_status_historico", "consultas", "avaliacoes", "prontuarios")) {
                    jdbc.update("DELETE FROM " + tabela + " WHERE organizacao_id = ?", organizacaoId);
                }
                jdbc.update("DELETE FROM pacientes WHERE organizacao_id = ?", organizacaoId);
                jdbc.update("DELETE FROM vinculos WHERE organizacao_id = ?", organizacaoId);
                jdbc.update("DELETE FROM organizacoes WHERE id = ?", organizacaoId);
            }
            jdbc.update("DELETE FROM usuarios WHERE id = ?", usuario.getId());
        });
    }
}
