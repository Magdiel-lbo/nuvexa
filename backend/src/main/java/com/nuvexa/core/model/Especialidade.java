package com.nuvexa.core.model;

import com.nuvexa.platform.util.ComRotulo;

/**
 * Vertical clínica habilitada para uma {@link Organizacao}. Dimensão independente de
 * {@link TipoOrganizacao} (que descreve a estrutura — individual ou clínica —, não a especialidade).
 * Nova vertical entra aqui como um novo valor.
 */
public enum Especialidade implements ComRotulo {
    NUTRICAO("Nutrição");

    private final String rotulo;

    Especialidade(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
