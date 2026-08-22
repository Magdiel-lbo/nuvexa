package com.nuvexa.nucleo.paciente.repository;

import com.nuvexa.nucleo.paciente.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
}
