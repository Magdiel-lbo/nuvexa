package com.nuvexa.nutricao.repository;

import com.nuvexa.nutricao.model.PerfilNutricional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilNutricionalRepository extends JpaRepository<PerfilNutricional, Long> {

    Optional<PerfilNutricional> findByPacienteId(Long pacienteId);

    Optional<PerfilNutricional> findByPacienteIdAndPacienteOrganizacaoId(Long pacienteId, Long organizacaoId);
}
