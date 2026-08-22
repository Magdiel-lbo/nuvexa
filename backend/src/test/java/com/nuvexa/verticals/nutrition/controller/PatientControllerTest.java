package com.nuvexa.verticals.nutrition.controller;

import tools.jackson.databind.ObjectMapper;
import com.nuvexa.platform.config.MessageConfig;
import com.nuvexa.verticals.nutrition.dto.request.PatientCreateRequestDTO;
import com.nuvexa.verticals.nutrition.dto.request.PatientUpdateRequestDTO;
import com.nuvexa.verticals.nutrition.dto.response.PatientResponseDTO;
import com.nuvexa.verticals.nutrition.model.ActivityLevel;
import com.nuvexa.core.patient.model.Gender;
import com.nuvexa.verticals.nutrition.model.Goal;
import com.nuvexa.platform.exception.CustomException;
import com.nuvexa.verticals.nutrition.service.PatientService;
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

@WebMvcTest(PatientController.class)
@Import(MessageConfig.class)
class PatientControllerTest {

    private static final String BASE_URL = "/api/v1/patients";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PatientService patientService;

    private PatientCreateRequestDTO validCreateRequest() {
        PatientCreateRequestDTO request = new PatientCreateRequestDTO();
        request.setName("Maria Souza");
        request.setBirthDate(LocalDate.of(1990, 5, 20));
        request.setGender(Gender.FEMALE);
        request.setHeight(new BigDecimal("1.65"));
        request.setWeight(new BigDecimal("62.50"));
        request.setGoal(Goal.LOSE_WEIGHT);
        request.setActivityLevel(ActivityLevel.MODERATELY_ACTIVE);
        return request;
    }

    private PatientResponseDTO responseFor(Long id, String name) {
        PatientResponseDTO response = new PatientResponseDTO();
        response.setId(id);
        response.setName(name);
        response.setBirthDate(LocalDate.of(1990, 5, 20));
        response.setGender(Gender.FEMALE);
        response.setHeight(new BigDecimal("1.65"));
        response.setWeight(new BigDecimal("62.50"));
        response.setGoal(Goal.LOSE_WEIGHT);
        response.setActivityLevel(ActivityLevel.MODERATELY_ACTIVE);
        return response;
    }

    @Test
    void shouldCreatePatientAndReturn201() throws Exception {
        when(patientService.create(any())).thenReturn(responseFor(1L, "Maria Souza"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Maria Souza"));
    }

    @Test
    void shouldReturn400WithPortugueseMessageWhenNameIsMissing() throws Exception {
        PatientCreateRequestDTO request = validCreateRequest();
        request.setName(null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Nome é obrigatório"))
                .andExpect(jsonPath("$.path").value(BASE_URL));
    }

    @Test
    void shouldReturn400WhenServiceRejectsBusinessRule() throws Exception {
        when(patientService.create(any()))
                .thenThrow(new CustomException(HttpStatus.BAD_REQUEST, "Peso deve ser maior que zero"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Peso deve ser maior que zero"));
    }

    private PatientUpdateRequestDTO validUpdateRequest() {
        PatientUpdateRequestDTO request = new PatientUpdateRequestDTO();
        request.setName("Maria Souza");
        request.setBirthDate(LocalDate.of(1990, 5, 20));
        request.setGender(Gender.FEMALE);
        request.setHeight(new BigDecimal("1.65"));
        request.setWeight(new BigDecimal("62.50"));
        request.setGoal(Goal.LOSE_WEIGHT);
        request.setActivityLevel(ActivityLevel.MODERATELY_ACTIVE);
        return request;
    }

    @Test
    void shouldFindPatientByIdAndReturn200() throws Exception {
        when(patientService.findById(1L)).thenReturn(responseFor(1L, "Maria Souza"));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Maria Souza"));
    }

    @Test
    void shouldUpdatePatientAndReturn200() throws Exception {
        when(patientService.update(eq(1L), any())).thenReturn(responseFor(1L, "Maria Atualizada"));

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria Atualizada"));
    }

    @Test
    void shouldReturn400WhenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get(BASE_URL + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn404WhenPatientNotFound() throws Exception {
        when(patientService.findById(99L))
                .thenThrow(new CustomException(HttpStatus.NOT_FOUND, "Paciente não encontrado: 99"));

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Paciente não encontrado: 99"));
    }

    @Test
    void shouldListPatients() throws Exception {
        when(patientService.findAll(eq("Maria"))).thenReturn(List.of(responseFor(1L, "Maria Souza")));

        mockMvc.perform(get(BASE_URL).param("search", "Maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Maria Souza"));
    }

    @Test
    void shouldDeletePatientAndReturn204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
    }
}
