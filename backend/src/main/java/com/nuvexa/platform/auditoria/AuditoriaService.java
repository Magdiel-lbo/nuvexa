package com.nuvexa.platform.auditoria;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Único ponto de escrita/leitura de {@link EventoAuditoria} — de propósito não expõe atualizar
 * nem excluir, então o log é append-only por construção (não por constraint de banco).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuditoriaService {

    private final EventoAuditoriaRepository eventoAuditoriaRepository;

    public void registrar(EntidadeAuditavel entidadeTipo, Long entidadeId, TipoEventoAuditoria tipoEvento,
                           Long organizacaoId, Long usuarioId, String usuarioNome, String dadosAntes, String dadosDepois) {
        EventoAuditoria evento = EventoAuditoria.builder()
                .organizacaoId(organizacaoId)
                .entidadeTipo(entidadeTipo)
                .entidadeId(entidadeId)
                .tipoEvento(tipoEvento)
                .usuarioId(usuarioId)
                .usuarioNome(usuarioNome)
                .dadosAntes(dadosAntes)
                .dadosDepois(dadosDepois)
                .build();
        eventoAuditoriaRepository.save(evento);
    }

    /** Acesso a dado clínico (leitura, download): igual a {@link #registrar}, mais o paciente afetado. */
    public void registrarAcesso(EntidadeAuditavel entidadeTipo, Long entidadeId, TipoEventoAuditoria tipoEvento,
                                 Long organizacaoId, Long usuarioId, String usuarioNome, Long pacienteId, String detalhe) {
        eventoAuditoriaRepository.save(EventoAuditoria.builder()
                .organizacaoId(organizacaoId)
                .entidadeTipo(entidadeTipo)
                .entidadeId(entidadeId)
                .tipoEvento(tipoEvento)
                .usuarioId(usuarioId)
                .usuarioNome(usuarioNome)
                .pacienteId(pacienteId)
                .dadosDepois(detalhe)
                .build());
    }

    public List<EventoAuditoria> listar(EntidadeAuditavel entidadeTipo, Long entidadeId, Long organizacaoId) {
        return eventoAuditoriaRepository.findByEntidadeTipoAndEntidadeIdAndOrganizacaoIdOrderByCriadoEmDesc(entidadeTipo, entidadeId, organizacaoId);
    }
}
