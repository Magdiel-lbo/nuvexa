-- Sessões de login. O refresh token nunca é guardado em claro: só o SHA-256. Cada renovação gera
-- uma nova linha na mesma família e marca a anterior como rotacionada; reutilizar um refresh já
-- rotacionado revoga a família inteira. O access token carrega o id da linha vigente (claim sid) e
-- o filtro JWT confere a linha a cada requisição, o que permite revogar na hora.

CREATE TABLE sessoes (
    id                  BIGSERIAL     PRIMARY KEY,
    usuario_id          BIGINT        NOT NULL,
    familia_id          VARCHAR(36)   NOT NULL,
    hash_refresh_token  VARCHAR(64)   NOT NULL,
    expira_em           TIMESTAMP     NOT NULL,
    criado_em           TIMESTAMP     NOT NULL DEFAULT now(),
    rotacionada_em      TIMESTAMP,
    revogada_em         TIMESTAMP,
    ip                  VARCHAR(64),
    user_agent          VARCHAR(255),
    CONSTRAINT uq_sessoes_hash_refresh_token UNIQUE (hash_refresh_token),
    CONSTRAINT fk_sessoes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_sessoes_usuario_id ON sessoes (usuario_id);
CREATE INDEX idx_sessoes_familia_id ON sessoes (familia_id);
