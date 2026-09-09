-- Log de auditoria genérico e reutilizável (platform/) — entidade_tipo/entidade_id são
-- polimórficos de propósito (sem FK), para não acoplar esta tabela a uma entidade clínica
-- específica. organizacao_id/usuario_id têm FK real aqui no banco, mesmo a entidade JPA
-- mapeando-os como colunas simples (ver com com.nuvexa.platform.auditoria.EventoAuditoria).

CREATE TABLE eventos_auditoria (
    id              BIGSERIAL     PRIMARY KEY,
    organizacao_id  BIGINT        NOT NULL,
    entidade_tipo   VARCHAR(30)   NOT NULL,
    entidade_id     BIGINT        NOT NULL,
    tipo_evento     VARCHAR(20)   NOT NULL,
    usuario_id      BIGINT        NOT NULL,
    usuario_nome    VARCHAR(150)  NOT NULL,
    dados_antes     TEXT,
    dados_depois    TEXT,
    criado_em       TIMESTAMP     NOT NULL DEFAULT now(),
    atualizado_em   TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_eventos_auditoria_organizacao FOREIGN KEY (organizacao_id) REFERENCES organizacoes(id),
    CONSTRAINT fk_eventos_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_eventos_auditoria_entidade ON eventos_auditoria (entidade_tipo, entidade_id);
CREATE INDEX idx_eventos_auditoria_organizacao_id ON eventos_auditoria (organizacao_id);
