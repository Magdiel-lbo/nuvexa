ALTER TABLE prontuarios ADD COLUMN assinado_por_id BIGINT REFERENCES usuarios(id);
ALTER TABLE prontuarios ADD COLUMN assinado_em TIMESTAMP;
