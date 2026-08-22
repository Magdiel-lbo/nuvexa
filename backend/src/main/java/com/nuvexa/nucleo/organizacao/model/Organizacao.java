package com.nuvexa.nucleo.organizacao.model;

import com.nuvexa.plataforma.persistencia.ModeloAbstrato;
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
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Organizacao extends ModeloAbstrato {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "legal_name", length = 150)
    private String legalName;

    @Column(length = 20)
    private String document;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoOrganizacao type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOrganizacao status;
}
