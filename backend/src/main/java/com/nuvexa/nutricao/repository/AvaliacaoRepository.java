package com.nuvexa.nutricao.repository;

import com.nuvexa.nutricao.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByIdAndOrganizacaoId(Long id, Long organizacaoId);
}
