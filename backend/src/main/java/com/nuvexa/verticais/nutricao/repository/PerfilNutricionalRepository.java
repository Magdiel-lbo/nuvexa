package com.nuvexa.verticais.nutricao.repository;

import com.nuvexa.verticais.nutricao.model.PerfilNutricional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilNutricionalRepository extends JpaRepository<PerfilNutricional, Long> {

    Optional<PerfilNutricional> findByPacienteId(Long pacienteId);
}
