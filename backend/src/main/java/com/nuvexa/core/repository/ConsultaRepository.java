package com.nuvexa.core.repository;

import com.nuvexa.core.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    Optional<Consulta> findByIdAndOrganizacaoId(Long id, Long organizacaoId);
}
