-- Auditoria de acesso a dado clínico (leitura/download): eventos_auditoria ganha o paciente
-- afetado. Nullable: eventos antigos (criação/edição/...) não o informam e continuam válidos.
ALTER TABLE eventos_auditoria ADD COLUMN paciente_id BIGINT;
ALTER TABLE eventos_auditoria
    ADD CONSTRAINT fk_eventos_auditoria_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id);
CREATE INDEX idx_eventos_auditoria_paciente_id ON eventos_auditoria (paciente_id);

-- Eventos de autenticação (login, falha, cadastro, redefinição de senha). Tabela própria: falha de
-- login não tem usuário nem organização, então não cabe em eventos_auditoria (ambos NOT NULL).
-- Append-only, sem atualizado_em. usuario_id é nulo quando o e-mail não existe.
CREATE TABLE eventos_autenticacao (
    id                BIGSERIAL     PRIMARY KEY,
    tipo_evento       VARCHAR(30)   NOT NULL,
    usuario_id        BIGINT,
    email_informado   VARCHAR(150),
    ip                VARCHAR(64),
    criado_em         TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_eventos_autenticacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_eventos_autenticacao_usuario_id ON eventos_autenticacao (usuario_id);
CREATE INDEX idx_eventos_autenticacao_email_criado_em ON eventos_autenticacao (email_informado, criado_em);
CREATE INDEX idx_eventos_autenticacao_ip_criado_em ON eventos_autenticacao (ip, criado_em);
