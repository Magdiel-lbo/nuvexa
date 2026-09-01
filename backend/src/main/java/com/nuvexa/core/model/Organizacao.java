package com.nuvexa.core.model;

import com.nuvexa.platform.persistence.ModeloAbstrato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "organizacoes")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Organizacao extends ModeloAbstrato {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "razao_social", length = 150)
    private String razaoSocial;

    @Column(length = 20)
    private String documento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoOrganizacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOrganizacao status;
}
