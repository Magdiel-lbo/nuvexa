package com.nuvexa.core.repository;

import com.nuvexa.core.model.ProntuarioAdendo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProntuarioAdendoRepository extends JpaRepository<ProntuarioAdendo, Long> {

    List<ProntuarioAdendo> findByProntuarioIdAndOrganizacaoIdOrderByCriadoEmAsc(Long prontuarioId, Long organizacaoId);

    boolean existsByProntuarioId(Long prontuarioId);
}
