-- Consulta passa a ter seu próprio profissional responsável, independente do(s) profissional(is)
-- vinculados ao paciente (ver paciente_profissionais) — decisão de domínio: consulta.profissional
-- não herda de paciente.
--
-- Coluna nasce NOT NULL, mas não há como escrever um valor fixo aqui (não existe "o" profissional
-- certo para um dado que já existia): qualquer consulta pré-existente é preenchida com o primeiro
-- profissional ativo da própria organização antes do ALTER COLUMN NOT NULL, para a migration não
-- quebrar em nenhum ambiente que já tenha alguma consulta de teste.

ALTER TABLE consultas ADD COLUMN profissional_id BIGINT;

UPDATE consultas c
SET profissional_id = (
    SELECT v.usuario_id
    FROM vinculos v
    WHERE v.organizacao_id = c.organizacao_id AND v.ativo = true
    ORDER BY v.id
    LIMIT 1
)
WHERE c.profissional_id IS NULL;

ALTER TABLE consultas ALTER COLUMN profissional_id SET NOT NULL;

ALTER TABLE consultas ADD CONSTRAINT fk_consultas_profissional FOREIGN KEY (profissional_id) REFERENCES usuarios(id);

CREATE INDEX idx_consultas_profissional_id ON consultas (profissional_id);
