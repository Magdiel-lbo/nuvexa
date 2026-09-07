-- Avaliação física (composição corporal: bioimpedância, antropometria, dobras cutâneas),
-- vertical nutrição. Escopada por organização desde o início, mesmo padrão de planos alimentares.

CREATE TABLE avaliacoes (
    id                  BIGSERIAL     PRIMARY KEY,
    organizacao_id      BIGINT        NOT NULL,
    paciente_id         BIGINT        NOT NULL,
    avaliador_id        BIGINT        NOT NULL,
    data                DATE          NOT NULL,
    tipo                VARCHAR(20)   NOT NULL,
    status              VARCHAR(20)   NOT NULL,
    peso                NUMERIC(5,2),
    percentual_gordura  NUMERIC(5,2),
    criado_em           TIMESTAMP     NOT NULL DEFAULT now(),
    atualizado_em       TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_avaliacoes_organizacao FOREIGN KEY (organizacao_id) REFERENCES organizacoes(id),
    CONSTRAINT fk_avaliacoes_paciente    FOREIGN KEY (paciente_id)    REFERENCES pacientes(id),
    CONSTRAINT fk_avaliacoes_avaliador   FOREIGN KEY (avaliador_id)   REFERENCES usuarios(id)
);

CREATE INDEX idx_avaliacoes_organizacao_id ON avaliacoes (organizacao_id);
CREATE INDEX idx_avaliacoes_paciente_id ON avaliacoes (paciente_id);
CREATE INDEX idx_avaliacoes_data ON avaliacoes (data);
