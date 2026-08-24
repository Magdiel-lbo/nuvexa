package com.nuvexa.core.vinculo.model;

import com.nuvexa.core.identity.model.Usuario;
import com.nuvexa.core.organizacao.model.Organizacao;
import com.nuvexa.platform.persistence.ModeloAbstrato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Associação entre um {@link Usuario} e uma {@link Organizacao}, com o papel que ele exerce ali.
 * Modelado como entidade de associação (e não como {@code Usuario.organizacaoId}) para que um
 * usuário possa pertencer a mais de uma organização no futuro sem mudança de schema.
 */
@Entity
@Table(name = "vinculos", uniqueConstraints = @UniqueConstraint(
        name = "uq_vinculos_usuario_organizacao",
        columnNames = {"usuario_id", "organizacao_id"}))
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Vinculo extends ModeloAbstrato {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private Organizacao organizacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PapelOrganizacional papel;

    @Column(nullable = false)
    private boolean ativo;
}
