package com.nuvexa.platform.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, Long> {

    List<EventoAuditoria> findByEntidadeTipoAndEntidadeIdAndOrganizacaoIdOrderByCriadoEmDesc(
            EntidadeAuditavel entidadeTipo, Long entidadeId, Long organizacaoId);
}
