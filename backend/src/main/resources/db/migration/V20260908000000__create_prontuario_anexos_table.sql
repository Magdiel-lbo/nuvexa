-- Documento anexado a um prontuário (exame, laudo etc.). Sem atualizado_em de propósito: o
-- anexo é imutável desde a criação, não existe fluxo de edição. chave_storage é o identificador
-- físico no storage (gerado pelo backend, nunca o nome original do arquivo).

CREATE TABLE prontuario_anexos (
    id               BIGSERIAL     PRIMARY KEY,
    organizacao_id   BIGINT        NOT NULL,
    prontuario_id    BIGINT        NOT NULL,
    nome_original    VARCHAR(255)  NOT NULL,
    chave_storage    VARCHAR(255)  NOT NULL UNIQUE,
    tipo_mime        VARCHAR(100)  NOT NULL,
    tamanho          BIGINT        NOT NULL,
    criado_por_id    BIGINT        NOT NULL,
    criado_em        TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_prontuario_anexos_organizacao FOREIGN KEY (organizacao_id) REFERENCES organizacoes(id),
    CONSTRAINT fk_prontuario_anexos_prontuario FOREIGN KEY (prontuario_id) REFERENCES prontuarios(id),
    CONSTRAINT fk_prontuario_anexos_criado_por FOREIGN KEY (criado_por_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_prontuario_anexos_prontuario_id ON prontuario_anexos (prontuario_id);
