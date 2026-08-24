-- Consulta (agendamento) real, substituindo o mock do frontend.
-- Escopada por organização desde o início — não repete o erro que a Fase 1 corrigiu depois
-- em pacientes.

CREATE TABLE consultas (
    id                BIGSERIAL     PRIMARY KEY,
    organizacao_id    BIGINT        NOT NULL,
    paciente_id       BIGINT        NOT NULL,
    data_hora         TIMESTAMP     NOT NULL,
    duracao_minutos   INTEGER       NOT NULL,
    tipo              VARCHAR(30)   NOT NULL,
    status            VARCHAR(20)   NOT NULL,
    observacoes       TEXT,
    criado_em         TIMESTAMP     NOT NULL DEFAULT now(),
    atualizado_em     TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_consultas_organizacao FOREIGN KEY (organizacao_id) REFERENCES organizacoes(id),
    CONSTRAINT fk_consultas_paciente    FOREIGN KEY (paciente_id)    REFERENCES pacientes(id)
);

CREATE INDEX idx_consultas_organizacao_id ON consultas (organizacao_id);
CREATE INDEX idx_consultas_paciente_id ON consultas (paciente_id);
CREATE INDEX idx_consultas_data_hora ON consultas (data_hora);
