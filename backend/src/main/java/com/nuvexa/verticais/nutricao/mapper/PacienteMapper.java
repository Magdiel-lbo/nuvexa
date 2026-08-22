package com.nuvexa.verticais.nutricao.mapper;

import com.nuvexa.nucleo.paciente.model.Paciente;
import com.nuvexa.verticais.nutricao.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.verticais.nutricao.dto.request.PacienteUpdateRequestDTO;
import com.nuvexa.verticais.nutricao.dto.response.PacienteResponseDTO;
import com.nuvexa.verticais.nutricao.model.PerfilNutricional;
import org.springframework.stereotype.Component;

@Component
public class PacienteMapper {

    public Paciente toPaciente(PacienteCreateRequestDTO request) {
        return Paciente.builder()
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .build();
    }

    public PerfilNutricional toPerfilNutricional(PacienteCreateRequestDTO request, Paciente paciente) {
        return PerfilNutricional.builder()
                .paciente(paciente)
                .height(request.getHeight())
                .weight(request.getWeight())
                .goal(request.getGoal())
                .activityLevel(request.getActivityLevel())
                .manualDailyCalories(request.getManualDailyCalories())
                .notes(request.getNotes())
                .build();
    }

    public void updatePaciente(PacienteUpdateRequestDTO request, Paciente paciente) {
        paciente.setName(request.getName());
        paciente.setBirthDate(request.getBirthDate());
        paciente.setGender(request.getGender());
    }

    public void updatePerfilNutricional(PacienteUpdateRequestDTO request, PerfilNutricional perfilNutricional) {
        perfilNutricional.setHeight(request.getHeight());
        perfilNutricional.setWeight(request.getWeight());
        perfilNutricional.setGoal(request.getGoal());
        perfilNutricional.setActivityLevel(request.getActivityLevel());
        perfilNutricional.setManualDailyCalories(request.getManualDailyCalories());
        perfilNutricional.setNotes(request.getNotes());
    }

    public PacienteResponseDTO toResponse(PerfilNutricional perfilNutricional) {
        Paciente paciente = perfilNutricional.getPaciente();
        return PacienteResponseDTO.builder()
                .id(paciente.getId())
                .name(paciente.getName())
                .birthDate(paciente.getBirthDate())
                .gender(paciente.getGender())
                .height(perfilNutricional.getHeight())
                .weight(perfilNutricional.getWeight())
                .goal(perfilNutricional.getGoal())
                .activityLevel(perfilNutricional.getActivityLevel())
                .manualDailyCalories(perfilNutricional.getManualDailyCalories())
                .notes(perfilNutricional.getNotes())
                .createdAt(perfilNutricional.getCreatedAt())
                .updatedAt(perfilNutricional.getUpdatedAt())
                .build();
    }
}
