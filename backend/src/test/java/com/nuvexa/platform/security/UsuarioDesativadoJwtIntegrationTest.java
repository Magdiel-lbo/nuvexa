package com.nuvexa.platform.security;

import com.jayway.jsonpath.JsonPath;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.OrganizacaoRepository;
import com.nuvexa.core.repository.UsuarioRepository;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.testsupport.LimpezaDeUsuarios;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Token válido (assinatura e expiração) de usuário desativado depois da emissão: o filtro JWT
 * precisa recusar com 401, sem esperar o token expirar. Contexto completo, sem transação ao redor.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioDesativadoJwtIntegrationTest {

    private static final String EMAIL = "jwt.desativado@nuvexa.test";
    private static final String ENDPOINT_AUTENTICADO = "/api/v1/contexto";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VinculoRepository vinculoRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbc;

    @AfterEach
    void limparUsuarioCriado() {
        usuarioRepository.findByEmail(EMAIL).ifPresent(usuario -> {
            List<Vinculo> vinculos = vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(usuario.getId());
            vinculoRepository.deleteAll(vinculos);
            vinculos.forEach(vinculo -> organizacaoRepository.deleteById(vinculo.getOrganizacao().getId()));
            LimpezaDeUsuarios.removerEventosDe(jdbc, usuario.getId());
            usuarioRepository.delete(usuario);
        });
    }

    @Test
    void tokenDeUsuarioAtivoAcessaEndpointAutenticado() throws Exception {
        String authorization = cadastrarEObterAuthorization();

        mockMvc.perform(get(ENDPOINT_AUTENTICADO).header("Authorization", authorization))
                .andExpect(status().isOk());
    }

    @Test
    void tokenValidoDeUsuarioDesativadoDeveSerRecusadoCom401() throws Exception {
        String authorization = cadastrarEObterAuthorization();
        mockMvc.perform(get(ENDPOINT_AUTENTICADO).header("Authorization", authorization))
                .andExpect(status().isOk());

        desativarUsuario();

        mockMvc.perform(get(ENDPOINT_AUTENTICADO).header("Authorization", authorization))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeUsuarioReativadoVoltaAFuncionar() throws Exception {
        String authorization = cadastrarEObterAuthorization();
        desativarUsuario();
        mockMvc.perform(get(ENDPOINT_AUTENTICADO).header("Authorization", authorization))
                .andExpect(status().isUnauthorized());

        Usuario usuario = usuarioRepository.findByEmail(EMAIL).orElseThrow();
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);

        mockMvc.perform(get(ENDPOINT_AUTENTICADO).header("Authorization", authorization))
                .andExpect(status().isOk());
    }

    @Test
    void tokenAdulteradoContinuaSendoRecusadoCom401() throws Exception {
        String authorization = cadastrarEObterAuthorization();

        String[] partes = authorization.split("\\.");
        char original = partes[2].charAt(5);
        partes[2] = partes[2].substring(0, 5) + (original == 'A' ? 'B' : 'A') + partes[2].substring(6);

        mockMvc.perform(get(ENDPOINT_AUTENTICADO).header("Authorization", String.join(".", partes)))
                .andExpect(status().isUnauthorized());
    }

    private void desativarUsuario() {
        Usuario usuario = usuarioRepository.findByEmail(EMAIL).orElseThrow();
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    private String cadastrarEObterAuthorization() throws Exception {
        String resposta = mockMvc.perform(post("/api/v1/auth/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teste Desativado\",\"email\":\"" + EMAIL + "\",\"senha\":\"senha12345\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(resposta, "$.token");
    }
}
