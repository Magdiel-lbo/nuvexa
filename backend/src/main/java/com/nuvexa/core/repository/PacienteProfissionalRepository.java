package com.nuvexa.core.repository;

import com.nuvexa.core.model.PacienteProfissional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PacienteProfissionalRepository extends JpaRepository<PacienteProfissional, Long> {

    Optional<PacienteProfissional> findByPacienteIdAndProfissionalId(Long pacienteId, Long profissionalId);
}
