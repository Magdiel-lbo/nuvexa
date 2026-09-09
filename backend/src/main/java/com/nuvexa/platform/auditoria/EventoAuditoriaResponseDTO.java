package com.nuvexa.platform.auditoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoAuditoriaResponseDTO {

    private Long id;

    private TipoEventoAuditoria tipoEvento;

    private Long usuarioId;

    private String usuarioNome;

    private String dadosAntes;

    private String dadosDepois;

    private LocalDateTime criadoEm;

    public static EventoAuditoriaResponseDTO from(EventoAuditoria evento) {
        return EventoAuditoriaResponseDTO.builder()
                .id(evento.getId())
                .tipoEvento(evento.getTipoEvento())
                .usuarioId(evento.getUsuarioId())
                .usuarioNome(evento.getUsuarioNome())
                .dadosAntes(evento.getDadosAntes())
                .dadosDepois(evento.getDadosDepois())
                .criadoEm(evento.getCriadoEm())
                .build();
    }
}
