-- Fundação multi-organização: vínculo Usuario<->Organizacao e escopo organizacional do Paciente.
-- Migration não destrutiva: nenhuma coluna/tabela é removida e os dados existentes são
-- preservados sob uma organização legada criada aqui.

-- 1. Documento da organização passa a ser único (NULLs continuam permitidos e não colidem).
ALTER TABLE organizacoes
    ADD CONSTRAINT uq_organizacoes_documento UNIQUE (documento);

-- 2. Vínculo organizacional: um usuário pode pertencer a várias organizações e vice-versa.
CREATE TABLE vinculos (
    id              BIGSERIAL    PRIMARY KEY,
    usuario_id      BIGINT       NOT NULL,
    organizacao_id  BIGINT       NOT NULL,
    papel           VARCHAR(20)  NOT NULL,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em       TIMESTAMP    NOT NULL DEFAULT now(),
    atualizado_em   TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_vinculos_usuario     FOREIGN KEY (usuario_id)     REFERENCES usuarios(id),
    CONSTRAINT fk_vinculos_organizacao FOREIGN KEY (organizacao_id) REFERENCES organizacoes(id),
    CONSTRAINT uq_vinculos_usuario_organizacao UNIQUE (usuario_id, organizacao_id)
);

CREATE INDEX idx_vinculos_usuario_id ON vinculos (usuario_id);
CREATE INDEX idx_vinculos_organizacao_id ON vinculos (organizacao_id);

-- 3. Coluna de escopo do paciente, inicialmente nullable para permitir o backfill abaixo.
ALTER TABLE pacientes
    ADD COLUMN organizacao_id BIGINT;

-- 4. Backfill dos dados existentes.
--    Cria uma organização legada apenas se já houver usuário ou paciente cadastrado,
--    e move todo o acervo atual para dentro dela.
DO $$
DECLARE
    organizacao_legada_id BIGINT;
    usuario_mais_antigo_id BIGINT;
BEGIN
    IF EXISTS (SELECT 1 FROM usuarios) OR EXISTS (SELECT 1 FROM pacientes) THEN

        INSERT INTO organizacoes (nome, tipo, status)
        VALUES ('Organização padrão', 'CLINICA', 'ATIVA')
        RETURNING id INTO organizacao_legada_id;

        UPDATE pacientes SET organizacao_id = organizacao_legada_id;

        -- O usuário mais antigo vira PROPRIETARIO da organização legada; os demais entram
        -- como MEMBRO. Antes desta migration todos tinham acesso global, então nenhum
        -- usuário perde acesso aos pacientes que já enxergava.
        SELECT id INTO usuario_mais_antigo_id FROM usuarios ORDER BY id ASC LIMIT 1;

        INSERT INTO vinculos (usuario_id, organizacao_id, papel, ativo)
        SELECT u.id,
               organizacao_legada_id,
               CASE WHEN u.id = usuario_mais_antigo_id THEN 'PROPRIETARIO' ELSE 'MEMBRO' END,
               TRUE
        FROM usuarios u;

    END IF;
END $$;

-- 5. Com o backfill feito, o escopo passa a ser obrigatório.
ALTER TABLE pacientes
    ALTER COLUMN organizacao_id SET NOT NULL;

ALTER TABLE pacientes
    ADD CONSTRAINT fk_pacientes_organizacao FOREIGN KEY (organizacao_id) REFERENCES organizacoes(id);

CREATE INDEX idx_pacientes_organizacao_id ON pacientes (organizacao_id);
