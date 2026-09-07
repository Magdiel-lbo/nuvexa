package com.nuvexa.nutricao.model;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Usuario;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "avaliacoes")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Avaliacao extends ModeloAbstrato {

    /** Escopo da avaliação: todo acesso é filtrado por esta organização. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private Organizacao organizacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    /** Profissional responsável pela avaliação — mesma regra de Prontuario.autor. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avaliador_id", nullable = false)
    private Usuario avaliador;

    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAvaliacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAvaliacao status;

    /** Nulo enquanto a avaliação estiver AGENDADA — ainda não há medidas. */
    @Column(precision = 5, scale = 2)
    private BigDecimal peso;

    @Column(name = "percentual_gordura", precision = 5, scale = 2)
    private BigDecimal percentualGordura;
}
