package com.nuvexa.platform.auditoria;

import com.nuvexa.platform.util.ComRotulo;

public enum TipoEventoAuditoria implements ComRotulo {
    CRIACAO("Criação"),
    EDICAO("Edição"),
    ASSINATURA("Assinatura"),
    ADENDO("Adendo"),
    EXCLUSAO("Exclusão"),
    UPLOAD_ANEXO("Anexo adicionado"),
    EXCLUSAO_ANEXO("Anexo excluído"),
    LEITURA("Leitura"),
    DOWNLOAD_ANEXO("Anexo baixado"),
    REVOGACAO("Revogação"),
    CONVITE_ACEITO("Convite aceito");

    private final String rotulo;

    TipoEventoAuditoria(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }
}
