package com.nuvexa.core.model;

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

/**
 * Fica em {@code core}, não em {@code nutricao}: registro clínico (anamnese/evolução/exames) não
 * é conceito exclusivo do domínio de nutrição — mesmo raciocínio que já mantém {@link Paciente}
 * e {@link Consulta} em {@code core}.
 */
@Entity
@Table(name = "prontuarios")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Prontuario extends ModeloAbstrato {

    /** Escopo do prontuário: todo acesso é filtrado por esta organização. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private Organizacao organizacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    /** Profissional autor/responsável por este registro — mesma regra de Consulta.profissional. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SecaoProntuario secao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusProntuario status;

    @Column(columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "com_anexo", nullable = false)
    private boolean comAnexo;
}
