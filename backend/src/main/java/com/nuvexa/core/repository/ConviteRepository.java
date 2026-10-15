package com.nuvexa.core.repository;

import com.nuvexa.core.model.Convite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConviteRepository extends JpaRepository<Convite, Long> {

    Optional<Convite> findByIdAndOrganizacaoId(Long id, Long organizacaoId);

    Optional<Convite> findByHashToken(String hashToken);

    List<Convite> findByOrganizacaoIdOrderByCriadoEmDesc(Long organizacaoId);

    List<Convite> findByOrganizacaoIdAndEmailAndUsadoEmIsNullAndRevogadoEmIsNull(Long organizacaoId, String email);
}
