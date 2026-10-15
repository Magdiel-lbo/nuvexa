package com.nuvexa.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Uma linha por refresh token emitido. {@code familiaId} agrupa as renovações de um mesmo login.
 * Está ativa enquanto não expirou, não foi revogada e não foi rotacionada.
 */
@Entity
@Table(name = "sessoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "familia_id", nullable = false, length = 36)
    private String familiaId;

    @Column(name = "hash_refresh_token", nullable = false, length = 64)
    private String hashRefreshToken;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "rotacionada_em")
    private LocalDateTime rotacionadaEm;

    @Column(name = "revogada_em")
    private LocalDateTime revogadaEm;

    @Column(length = 64)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;
}
