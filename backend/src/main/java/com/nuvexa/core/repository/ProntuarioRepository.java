package com.nuvexa.core.repository;

import com.nuvexa.core.model.Prontuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {

    Optional<Prontuario> findByIdAndOrganizacaoId(Long id, Long organizacaoId);
}
