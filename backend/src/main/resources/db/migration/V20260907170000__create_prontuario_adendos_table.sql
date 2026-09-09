-- Correção/complemento a um prontuário já ASSINADO. Sem atualizado_em de propósito: o adendo é
-- imutável desde a criação, não existe fluxo de edição.

CREATE TABLE prontuario_adendos (
    id              BIGSERIAL     PRIMARY KEY,
    organizacao_id  BIGINT        NOT NULL,
    prontuario_id   BIGINT        NOT NULL,
    autor_id        BIGINT        NOT NULL,
    texto           TEXT          NOT NULL,
    criado_em       TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_prontuario_adendos_organizacao FOREIGN KEY (organizacao_id) REFERENCES organizacoes(id),
    CONSTRAINT fk_prontuario_adendos_prontuario FOREIGN KEY (prontuario_id) REFERENCES prontuarios(id),
    CONSTRAINT fk_prontuario_adendos_autor FOREIGN KEY (autor_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_prontuario_adendos_prontuario_id ON prontuario_adendos (prontuario_id);
