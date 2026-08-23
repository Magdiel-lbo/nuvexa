package com.nuvexa.verticals.nutricao.controller;

import tools.jackson.databind.ObjectMapper;
import com.nuvexa.platform.config.MessageConfig;
import com.nuvexa.verticals.nutricao.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.verticals.nutricao.dto.request.PacienteUpdateRequestDTO;
import com.nuvexa.verticals.nutricao.dto.response.PacienteResponseDTO;
import com.nuvexa.verticals.nutricao.model.NivelAtividade;
import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.verticals.nutricao.model.Objetivo;
import com.nuvexa.platform.exception.NegocioException;
import com.nuvexa.verticals.nutricao.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PacienteController.class)
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
        PacienteCreateRequestDTO request = new PacienteCreateRequestDTO();
        request.setNome("Maria Souza");
        request.setDataNascimento(LocalDate.of(1990, 5, 20));
        request.setSexo(Sexo.FEMININO);
        request.setAltura(new BigDecimal("1.65"));
        request.setPeso(new BigDecimal("62.50"));
        request.setObjetivo(Objetivo.EMAGRECIMENTO);
        request.setNivelAtividade(NivelAtividade.MODERADAMENTE_ATIVO);
        return request;
    }

    private PacienteResponseDTO responseFor(Long id, String name) {
        PacienteResponseDTO response = new PacienteResponseDTO();
        response.setId(id);
        response.setNome(name);
        response.setDataNascimento(LocalDate.of(1990, 5, 20));
        response.setSexo(Sexo.FEMININO);
        response.setAltura(new BigDecimal("1.65"));
        response.setPeso(new BigDecimal("62.50"));
        response.setObjetivo(Objetivo.EMAGRECIMENTO);
        response.setNivelAtividade(NivelAtividade.MODERADAMENTE_ATIVO);
        return response;
    }

    @Test
    void shouldCreatePatientAndReturn201() throws Exception {
        when(pacienteService.create(any())).thenReturn(responseFor(1L, "Maria Souza"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Souza"));
    }

    @Test
    void shouldReturn400WithPortugueseMessageWhenNameIsMissing() throws Exception {
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
    void shouldReturn400WhenServiceRejectsBusinessRule() throws Exception {
        when(pacienteService.create(any()))
                .thenThrow(new NegocioException(HttpStatus.BAD_REQUEST, "Peso deve ser maior que zero"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Peso deve ser maior que zero"));
    }

    private PacienteUpdateRequestDTO validUpdateRequest() {
        PacienteUpdateRequestDTO request = new PacienteUpdateRequestDTO();
        request.setNome("Maria Souza");
        request.setDataNascimento(LocalDate.of(1990, 5, 20));
        request.setSexo(Sexo.FEMININO);
        request.setAltura(new BigDecimal("1.65"));
        request.setPeso(new BigDecimal("62.50"));
        request.setObjetivo(Objetivo.EMAGRECIMENTO);
        request.setNivelAtividade(NivelAtividade.MODERADAMENTE_ATIVO);
        return request;
    }

    @Test
    void shouldFindPatientByIdAndReturn200() throws Exception {
        when(pacienteService.findById(1L)).thenReturn(responseFor(1L, "Maria Souza"));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Souza"));
    }

    @Test
    void shouldUpdatePatientAndReturn200() throws Exception {
        when(pacienteService.update(eq(1L), any())).thenReturn(responseFor(1L, "Maria Atualizada"));

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Atualizada"));
    }

    @Test
    void shouldReturn400WhenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get(BASE_URL + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn404WhenPatientNotFound() throws Exception {
        when(pacienteService.findById(99L))
                .thenThrow(new NegocioException(HttpStatus.NOT_FOUND, "Paciente não encontrado: 99"));

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Paciente não encontrado: 99"));
    }

    @Test
    void shouldListPatients() throws Exception {
        when(pacienteService.findAll(eq("Maria"))).thenReturn(List.of(responseFor(1L, "Maria Souza")));

        mockMvc.perform(get(BASE_URL).param("busca", "Maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Maria Souza"));
    }

    @Test
    void shouldDeletePatientAndReturn204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
    }
}
