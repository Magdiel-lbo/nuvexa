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

import java.time.LocalDate;

@Entity
@Table(name = "planos_alimentares")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PlanoAlimentar extends ModeloAbstrato {

    /** Escopo do plano: todo acesso é filtrado por esta organização. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private Organizacao organizacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    /** Profissional autor/responsável pelo plano — mesma regra de Consulta.profissional. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private Integer calorias;

    @Column(name = "refeicoes_por_dia", nullable = false)
    private Integer refeicoesPorDia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPlanoAlimentar status;
}
