package com.nuvexa.core.model;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.platform.persistence.ModeloAbstrato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Fica em {@code core}, não em {@code nutricao}: agendamento não é conceito exclusivo do
 * domínio de nutrição — o mesmo raciocínio que já mantém {@link Paciente} em {@code core}.
 */
@Entity
@Table(name = "consultas")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Consulta extends ModeloAbstrato {

    /** Escopo da consulta: todo acesso é filtrado por esta organização. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private Organizacao organizacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    /**
     * Profissional responsável por esta consulta. Independente do(s) profissional(is)
     * vinculados ao paciente ({@link PacienteProfissional}) — não herda de {@link #paciente}.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Usuario profissional;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoConsulta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConsulta status;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}
