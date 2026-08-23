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
                .nome(request.getNome())
                .dataNascimento(request.getDataNascimento())
                .sexo(request.getSexo())
                .build();
    }

    public PerfilNutricional toPerfilNutricional(PacienteCreateRequestDTO request, Paciente paciente) {
        return PerfilNutricional.builder()
                .paciente(paciente)
                .altura(request.getAltura())
                .peso(request.getPeso())
                .objetivo(request.getObjetivo())
                .nivelAtividade(request.getNivelAtividade())
                .caloriasDiariasManuais(request.getCaloriasDiariasManuais())
                .observacoes(request.getObservacoes())
                .build();
    }

    public void updatePaciente(PacienteUpdateRequestDTO request, Paciente paciente) {
        paciente.setNome(request.getNome());
        paciente.setDataNascimento(request.getDataNascimento());
        paciente.setSexo(request.getSexo());
    }

    public void updatePerfilNutricional(PacienteUpdateRequestDTO request, PerfilNutricional perfilNutricional) {
        perfilNutricional.setAltura(request.getAltura());
        perfilNutricional.setPeso(request.getPeso());
        perfilNutricional.setObjetivo(request.getObjetivo());
        perfilNutricional.setNivelAtividade(request.getNivelAtividade());
        perfilNutricional.setCaloriasDiariasManuais(request.getCaloriasDiariasManuais());
        perfilNutricional.setObservacoes(request.getObservacoes());
    }

    public PacienteResponseDTO toResponse(PerfilNutricional perfilNutricional) {
        Paciente paciente = perfilNutricional.getPaciente();
        return PacienteResponseDTO.builder()
                .id(paciente.getId())
                .nome(paciente.getNome())
                .dataNascimento(paciente.getDataNascimento())
                .sexo(paciente.getSexo())
                .altura(perfilNutricional.getAltura())
                .peso(perfilNutricional.getPeso())
                .objetivo(perfilNutricional.getObjetivo())
                .nivelAtividade(perfilNutricional.getNivelAtividade())
                .caloriasDiariasManuais(perfilNutricional.getCaloriasDiariasManuais())
                .observacoes(perfilNutricional.getObservacoes())
                .criadoEm(perfilNutricional.getCriadoEm())
                .atualizadoEm(perfilNutricional.getAtualizadoEm())
                .build();
    }
}
