package com.nuvexa.core.consulta.repository;

import com.nuvexa.core.consulta.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    Optional<Consulta> findByIdAndOrganizacaoId(Long id, Long organizacaoId);
}
