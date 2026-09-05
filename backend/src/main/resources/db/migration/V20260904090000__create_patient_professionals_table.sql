-- Vínculo N:N entre Paciente e Usuario (profissional): um paciente pode ter mais de um
-- profissional responsável dentro da mesma organização. Escopada por organização desde o
-- início, mesmo padrão de "consultas".

CREATE TABLE paciente_profissionais (
    id                BIGSERIAL     PRIMARY KEY,
    paciente_id       BIGINT        NOT NULL,
    profissional_id   BIGINT        NOT NULL,
    organizacao_id    BIGINT        NOT NULL,
    ativo             BOOLEAN       NOT NULL DEFAULT TRUE,
    criado_em         TIMESTAMP     NOT NULL DEFAULT now(),
    atualizado_em     TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_paciente_profissionais_paciente     FOREIGN KEY (paciente_id)     REFERENCES pacientes(id),
    CONSTRAINT fk_paciente_profissionais_profissional FOREIGN KEY (profissional_id) REFERENCES usuarios(id),
    CONSTRAINT fk_paciente_profissionais_organizacao  FOREIGN KEY (organizacao_id)  REFERENCES organizacoes(id),
    CONSTRAINT uq_paciente_profissionais_paciente_profissional UNIQUE (paciente_id, profissional_id)
);

CREATE INDEX idx_paciente_profissionais_paciente_id ON paciente_profissionais (paciente_id);
CREATE INDEX idx_paciente_profissionais_profissional_id ON paciente_profissionais (profissional_id);
CREATE INDEX idx_paciente_profissionais_organizacao_id ON paciente_profissionais (organizacao_id);
