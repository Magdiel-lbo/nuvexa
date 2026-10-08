package com.nuvexa.core.repository;

import com.nuvexa.core.model.ConsultaStatusHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultaStatusHistoricoRepository extends JpaRepository<ConsultaStatusHistorico, Long> {

    List<ConsultaStatusHistorico> findByConsultaIdAndOrganizacaoIdOrderByCriadoEmAsc(Long consultaId, Long organizacaoId);

    /**
     * Só conta transições reais (statusAnterior preenchido) — a linha de criação (null → status
     * inicial) sozinha não deve bloquear exclusão, só uma consulta que de fato mudou de status em
     * algum momento. Decisão confirmada: ver {@code ConsultaService.garantirSemHistorico}.
     */
    boolean existsByConsultaIdAndStatusAnteriorIsNotNull(Long consultaId);
}
