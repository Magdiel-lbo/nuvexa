package com.nuvexa.core.consulta.model;

import com.nuvexa.core.organizacao.model.Organizacao;
import com.nuvexa.core.paciente.model.Paciente;
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
 * Fica em {@code core}, não em {@code verticals.nutricao}: agendamento não é conceito exclusivo
 * de uma vertical — o mesmo raciocínio que já mantém {@link Paciente} fora da vertical.
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
