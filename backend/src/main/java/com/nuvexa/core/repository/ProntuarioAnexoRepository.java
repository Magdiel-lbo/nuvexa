package com.nuvexa.core.repository;

import com.nuvexa.core.model.ProntuarioAnexo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProntuarioAnexoRepository extends JpaRepository<ProntuarioAnexo, Long> {

    List<ProntuarioAnexo> findByProntuarioIdAndOrganizacaoIdOrderByCriadoEmAsc(Long prontuarioId, Long organizacaoId);

    /**
     * Três condições combinadas de propósito: um anexo só é resolvido se pertencer ao mesmo
     * prontuário E à mesma organização informados — nunca só pelo próprio id.
     */
    Optional<ProntuarioAnexo> findByIdAndProntuarioIdAndOrganizacaoId(Long id, Long prontuarioId, Long organizacaoId);

    boolean existsByProntuarioId(Long prontuarioId);
}
