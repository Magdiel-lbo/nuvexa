-- Plano alimentar (plano nutricional do paciente: calorias-alvo, refeições por dia, status),
-- vertical nutrição. Escopado por organização desde o início, mesmo padrão de prontuários.

CREATE TABLE planos_alimentares (
    id                  BIGSERIAL     PRIMARY KEY,
    organizacao_id      BIGINT        NOT NULL,
    paciente_id         BIGINT        NOT NULL,
    autor_id            BIGINT        NOT NULL,
    nome                VARCHAR(150)  NOT NULL,
    data_inicio         DATE          NOT NULL,
    calorias            INTEGER       NOT NULL,
    refeicoes_por_dia   INTEGER       NOT NULL,
    status              VARCHAR(20)   NOT NULL,
    criado_em           TIMESTAMP     NOT NULL DEFAULT now(),
    atualizado_em       TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_planos_alimentares_organizacao FOREIGN KEY (organizacao_id) REFERENCES organizacoes(id),
    CONSTRAINT fk_planos_alimentares_paciente    FOREIGN KEY (paciente_id)    REFERENCES pacientes(id),
    CONSTRAINT fk_planos_alimentares_autor       FOREIGN KEY (autor_id)       REFERENCES usuarios(id)
);

CREATE INDEX idx_planos_alimentares_organizacao_id ON planos_alimentares (organizacao_id);
CREATE INDEX idx_planos_alimentares_paciente_id ON planos_alimentares (paciente_id);
CREATE INDEX idx_planos_alimentares_atualizado_em ON planos_alimentares (atualizado_em);
