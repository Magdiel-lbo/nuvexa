package com.nuvexa.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Uma linha por transição de status de {@link Consulta} — nunca uma tabela por status, nunca um
 * histórico genérico. Histórico de domínio estruturado e consultável, complementar a
 * {@link com.nuvexa.platform.auditoria.EventoAuditoria} (que hoje nem cobre Consulta) — este aqui
 * responde perguntas de negócio (ex.: "quantas faltas esse paciente teve"), o outro é o mecanismo
 * de compliance/rastreabilidade genérico. Igual a {@link ProntuarioAdendo}/{@link ProntuarioAnexo},
 * não estende {@link com.nuvexa.platform.persistence.ModeloAbstrato} de propósito: é imutável
 * desde a criação, nunca é atualizado.
 *
 * <p>{@code statusAnterior} é {@code null} só na linha que representa a criação da consulta
 * (null → status inicial); toda transição posterior sempre tem os dois lados preenchidos.
 */
@Entity
@Table(name = "consulta_status_historico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaStatusHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consulta_id", nullable = false)
    private Consulta consulta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private Organizacao organizacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 20)
    private StatusConsulta statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 20)
    private StatusConsulta statusNovo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alterado_por_id", nullable = false)
    private Usuario alteradoPor;

    @Column(name = "motivo_transicao", columnDefinition = "TEXT")
    private String motivoTransicao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }
}
