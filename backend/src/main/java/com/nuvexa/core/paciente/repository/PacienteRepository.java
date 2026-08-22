package com.nuvexa.core.paciente.repository;

import com.nuvexa.core.paciente.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
}
