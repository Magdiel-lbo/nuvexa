-- Convites para entrar em uma organização. O token nunca é guardado em claro: só o SHA-256 dele
-- (mesmo padrão do token de redefinição de senha). Uso único (usado_em) e revogável (revogado_em);
-- sem ON DELETE CASCADE, coerente com o restante do schema.

CREATE TABLE convites (
    id                  BIGSERIAL     PRIMARY KEY,
    organizacao_id      BIGINT        NOT NULL,
    email               VARCHAR(150)  NOT NULL,
    papel               VARCHAR(20)   NOT NULL,
    hash_token          VARCHAR(64)   NOT NULL,
    expira_em           TIMESTAMP     NOT NULL,
    convidado_por_id    BIGINT        NOT NULL,
    usado_em            TIMESTAMP,
    revogado_em         TIMESTAMP,
    criado_em           TIMESTAMP     NOT NULL DEFAULT now(),
    atualizado_em       TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT uq_convites_hash_token UNIQUE (hash_token),
    CONSTRAINT fk_convites_organizacao FOREIGN KEY (organizacao_id) REFERENCES organizacoes(id),
    CONSTRAINT fk_convites_convidado_por FOREIGN KEY (convidado_por_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_convites_organizacao_id ON convites (organizacao_id);
CREATE INDEX idx_convites_organizacao_email ON convites (organizacao_id, email);
