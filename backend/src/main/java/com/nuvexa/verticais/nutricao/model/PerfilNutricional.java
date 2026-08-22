package com.nuvexa.verticais.nutricao.model;

import com.nuvexa.core.paciente.model.Paciente;
import com.nuvexa.plataforma.persistencia.ModeloAbstrato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "perfis_nutricionais")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PerfilNutricional extends ModeloAbstrato {

    @OneToOne
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal altura;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal peso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Objetivo objetivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_atividade", nullable = false, length = 30)
    private NivelAtividade nivelAtividade;

    @Column(name = "calorias_diarias_manuais", precision = 6, scale = 2)
    private BigDecimal caloriasDiariasManuais;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}
