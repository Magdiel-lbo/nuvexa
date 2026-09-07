package com.nuvexa.nutricao.repository;

import com.nuvexa.nutricao.model.PlanoAlimentar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanoAlimentarRepository extends JpaRepository<PlanoAlimentar, Long> {

    Optional<PlanoAlimentar> findByIdAndOrganizacaoId(Long id, Long organizacaoId);
}
