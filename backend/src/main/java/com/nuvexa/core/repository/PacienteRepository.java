package com.nuvexa.core.repository;

import com.nuvexa.core.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByIdAndOrganizacaoId(Long id, Long organizacaoId);
}
