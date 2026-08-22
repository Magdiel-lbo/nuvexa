package com.nuvexa.verticals.nutricao.mapper;

import com.nuvexa.core.paciente.model.Paciente;
import com.nuvexa.verticals.nutricao.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.verticals.nutricao.dto.request.PacienteUpdateRequestDTO;
import com.nuvexa.verticals.nutricao.dto.response.PacienteResponseDTO;
import com.nuvexa.verticals.nutricao.model.PerfilNutricional;
import org.springframework.stereotype.Component;

@Component
public class PacienteMapper {

    public Paciente toPaciente(PacienteCreateRequestDTO request) {
        return Paciente.builder()
                .nome(request.getName())
                .dataNascimento(request.getBirthDate())
                .sexo(request.getGender())
                .build();
    }

    public PerfilNutricional toPerfilNutricional(PacienteCreateRequestDTO request, Paciente paciente) {
        return PerfilNutricional.builder()
                .paciente(paciente)
                .altura(request.getHeight())
                .peso(request.getWeight())
                .objetivo(request.getGoal())
                .nivelAtividade(request.getActivityLevel())
                .caloriasDiariasManuais(request.getManualDailyCalories())
                .observacoes(request.getNotes())
                .build();
    }

    public void updatePaciente(PacienteUpdateRequestDTO request, Paciente paciente) {
        paciente.setNome(request.getName());
        paciente.setDataNascimento(request.getBirthDate());
        paciente.setSexo(request.getGender());
    }

    public void updatePerfilNutricional(PacienteUpdateRequestDTO request, PerfilNutricional perfilNutricional) {
        perfilNutricional.setAltura(request.getHeight());
        perfilNutricional.setPeso(request.getWeight());
        perfilNutricional.setObjetivo(request.getGoal());
        perfilNutricional.setNivelAtividade(request.getActivityLevel());
        perfilNutricional.setCaloriasDiariasManuais(request.getManualDailyCalories());
        perfilNutricional.setObservacoes(request.getNotes());
    }

    public PacienteResponseDTO toResponse(PerfilNutricional perfilNutricional) {
        Paciente paciente = perfilNutricional.getPaciente();
        return PacienteResponseDTO.builder()
                .id(paciente.getId())
                .name(paciente.getNome())
                .birthDate(paciente.getDataNascimento())
                .gender(paciente.getSexo())
                .height(perfilNutricional.getAltura())
                .weight(perfilNutricional.getPeso())
                .goal(perfilNutricional.getObjetivo())
                .activityLevel(perfilNutricional.getNivelAtividade())
                .manualDailyCalories(perfilNutricional.getCaloriasDiariasManuais())
                .notes(perfilNutricional.getObservacoes())
                .createdAt(perfilNutricional.getCriadoEm())
                .updatedAt(perfilNutricional.getAtualizadoEm())
                .build();
    }
}
