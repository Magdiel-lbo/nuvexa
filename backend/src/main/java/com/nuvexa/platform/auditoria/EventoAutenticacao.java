package com.nuvexa.platform.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/** Evento de autenticação — append-only (sem atualizado_em, sem update/delete no service). */
@Entity
@Table(name = "eventos_autenticacao")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoAutenticacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 30)
    private TipoEventoAutenticacao tipoEvento;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "email_informado", length = 150)
    private String emailInformado;

    @Column(length = 64)
    private String ip;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void onCreate() {
        criadoEm = LocalDateTime.now();
    }
}
