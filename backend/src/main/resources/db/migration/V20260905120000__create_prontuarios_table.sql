-- Prontuário (registro clínico: anamnese/evolução/exames) real, substituindo o placeholder do
-- frontend. Escopado por organização desde o início, mesmo padrão de consultas.

CREATE TABLE prontuarios (
    id                BIGSERIAL     PRIMARY KEY,
    organizacao_id    BIGINT        NOT NULL,
    paciente_id       BIGINT        NOT NULL,
    autor_id          BIGINT        NOT NULL,
    secao             VARCHAR(20)   NOT NULL,
    status            VARCHAR(20)   NOT NULL,
    conteudo          TEXT,
    com_anexo         BOOLEAN       NOT NULL DEFAULT false,
    criado_em         TIMESTAMP     NOT NULL DEFAULT now(),
    atualizado_em     TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_prontuarios_organizacao FOREIGN KEY (organizacao_id) REFERENCES organizacoes(id),
    CONSTRAINT fk_prontuarios_paciente    FOREIGN KEY (paciente_id)    REFERENCES pacientes(id),
    CONSTRAINT fk_prontuarios_autor       FOREIGN KEY (autor_id)       REFERENCES usuarios(id)
);

CREATE INDEX idx_prontuarios_organizacao_id ON prontuarios (organizacao_id);
CREATE INDEX idx_prontuarios_paciente_id ON prontuarios (paciente_id);
CREATE INDEX idx_prontuarios_atualizado_em ON prontuarios (atualizado_em);
