package com.nuvexa.core.model;

import com.nuvexa.platform.persistence.ModeloAbstrato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Associação entre um {@link Paciente} e o {@link Usuario} (profissional) responsável por ele
 * dentro de uma organização. Um paciente pode ter mais de um profissional vinculado; a mesma
 * organização é sempre a do vínculo do profissional que criou a associação — replicada aqui
 * (e não derivada via paciente/profissional) pelo mesmo motivo de {@link Paciente#getOrganizacao()}
 * e {@link Consulta#getOrganizacao()}: escopo direto e simples de filtrar em query.
 *
 * <p><strong>Não define escopo de autorização.</strong> A regra de produto é que qualquer usuário
 * com vínculo ativo na organização acessa todos os pacientes dela — ver
 * {@link com.nuvexa.core.service.ValidadorOrganizacional}. Esta associação registra só quem é o
 * profissional responsável por um paciente, sem restringir o acesso de outros profissionais da
 * mesma organização a ele.
 */
@Entity
@Table(name = "paciente_profissionais", uniqueConstraints = @UniqueConstraint(
        name = "uq_paciente_profissionais_paciente_profissional",
        columnNames = {"paciente_id", "profissional_id"}))
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PacienteProfissional extends ModeloAbstrato {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Usuario profissional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private Organizacao organizacao;

    @Column(nullable = false)
    private boolean ativo;
}
