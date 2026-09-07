package com.nuvexa.core.controller;

import tools.jackson.databind.ObjectMapper;
import com.nuvexa.core.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.core.dto.request.PacienteUpdateRequestDTO;
import com.nuvexa.core.dto.response.PacienteResponseDTO;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.core.service.PacienteService;
import com.nuvexa.platform.config.MessageConfig;
import com.nuvexa.platform.exception.NegocioException;
import com.nuvexa.platform.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Slice puro de web: o JwtAuthenticationFilter é um bean Filter e seria arrastado para dentro
// da fatia (sem o JwtService, que não faz parte dela), então fica explicitamente de fora.
// Autorização de verdade é exercida em PacienteAutorizacaoIntegrationTest, com o contexto completo.
@WebMvcTest(value = PacienteController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(MessageConfig.class)
class PacienteControllerTest {

    private static final String BASE_URL = "/api/v1/pacientes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PacienteService pacienteService;

    private PacienteCreateRequestDTO validCreateRequest() {
        return PacienteCreateRequestDTO.builder()
                .nome("Maria Souza")
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build();
    }

    private PacienteResponseDTO responseFor(Long id, String nome) {
        return PacienteResponseDTO.builder()
                .id(id)
                .nome(nome)
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .idade(35)
                .build();
    }

    @Test
    void deveCriarPacienteERetornar201() throws Exception {
        when(pacienteService.create(any())).thenReturn(responseFor(1L, "Maria Souza"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Souza"));
    }

    @Test
    void deveRetornar400ComMensagemEmPortuguesQuandoNomeAusente() throws Exception {
        PacienteCreateRequestDTO request = validCreateRequest();
        request.setNome(null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Nome é obrigatório"))
                .andExpect(jsonPath("$.caminho").value(BASE_URL));
    }

    @Test
    void deveRetornar400QuandoServiceRejeitaRegraDeNegocio() throws Exception {
        when(pacienteService.create(any()))
                .thenThrow(new NegocioException(HttpStatus.BAD_REQUEST, "Data de nascimento não pode ser futura"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Data de nascimento não pode ser futura"));
    }

    private PacienteUpdateRequestDTO validUpdateRequest() {
        return PacienteUpdateRequestDTO.builder()
                .nome("Maria Souza")
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build();
    }

    @Test
    void deveBuscarPacientePorIdERetornar200() throws Exception {
        when(pacienteService.findById(1L)).thenReturn(responseFor(1L, "Maria Souza"));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Souza"));
    }

    @Test
    void deveAtualizarPacienteERetornar200() throws Exception {
        when(pacienteService.update(eq(1L), any())).thenReturn(responseFor(1L, "Maria Atualizada"));

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Atualizada"));
    }

    @Test
    void deveRetornar400QuandoIdNaoNumerico() throws Exception {
        mockMvc.perform(get(BASE_URL + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deveRetornar404QuandoPacienteNaoEncontrado() throws Exception {
        when(pacienteService.findById(99L))
                .thenThrow(new NegocioException(HttpStatus.NOT_FOUND, "Paciente não encontrado: 99"));

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Paciente não encontrado: 99"));
    }

    @Test
    void deveListarPacientes() throws Exception {
        when(pacienteService.findAll(eq("Maria"))).thenReturn(List.of(responseFor(1L, "Maria Souza")));

        mockMvc.perform(get(BASE_URL).param("busca", "Maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Maria Souza"));
    }
}
