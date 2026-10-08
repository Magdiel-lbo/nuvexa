-- Histórico de transições de status de Consulta — uma linha por transição (nunca uma tabela por
-- status, nunca um histórico genérico). Sem atualizado_em de propósito: imutável desde a criação,
-- mesmo padrão de prontuario_adendos/prontuario_anexos. Sem ON DELETE CASCADE em nenhuma FK,
-- coerente com o restante do schema.

CREATE TABLE consulta_status_historico (
    id                  BIGSERIAL     PRIMARY KEY,
    consulta_id         BIGINT        NOT NULL,
    organizacao_id      BIGINT        NOT NULL,
    status_anterior     VARCHAR(20),
    status_novo         VARCHAR(20)   NOT NULL,
    alterado_por_id     BIGINT        NOT NULL,
    motivo_transicao    TEXT,
    criado_em           TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_consulta_status_historico_consulta FOREIGN KEY (consulta_id) REFERENCES consultas(id),
    CONSTRAINT fk_consulta_status_historico_organizacao FOREIGN KEY (organizacao_id) REFERENCES organizacoes(id),
    CONSTRAINT fk_consulta_status_historico_alterado_por FOREIGN KEY (alterado_por_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_consulta_status_historico_consulta_id ON consulta_status_historico (consulta_id);
CREATE INDEX idx_consulta_status_historico_organizacao_id ON consulta_status_historico (organizacao_id);
