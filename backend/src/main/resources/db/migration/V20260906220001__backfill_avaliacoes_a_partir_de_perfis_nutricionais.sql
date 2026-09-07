-- Backfill: cria a primeira Avaliacao de cada paciente que já tinha peso registrado no perfil
-- nutricional, mas ainda nenhuma avaliação histórica. Idempotente via NOT EXISTS — seguro rodar
-- mais de uma vez (na segunda execução nenhum paciente mais se qualifica).
--
-- Avaliador: profissional único vinculado ao paciente (paciente_profissionais ativo), com
-- fallback para o PROPRIETARIO ativo mais antigo da organização quando não houver exatamente um
-- profissional vinculado. Perfis cujo avaliador não puder ser resolvido por nenhuma das duas
-- regras ficam de fora do INSERT (não bloqueiam a migration nem violam o NOT NULL de
-- avaliador_id) — validar manualmente depois com:
--
--   SELECT p.id, p.nome FROM pacientes p JOIN perfis_nutricionais pn ON pn.paciente_id = p.id
--   WHERE NOT EXISTS (SELECT 1 FROM avaliacoes a WHERE a.paciente_id = p.id);
--
-- Essa consulta deve retornar zero linhas após esta migration.

WITH candidatos AS (
    SELECT
        p.organizacao_id AS organizacao_id,
        pn.paciente_id   AS paciente_id,
        COALESCE(
            (SELECT pp.profissional_id
               FROM paciente_profissionais pp
              WHERE pp.paciente_id = pn.paciente_id AND pp.ativo = true
              GROUP BY pp.profissional_id
             HAVING (SELECT COUNT(*) FROM paciente_profissionais pp2
                      WHERE pp2.paciente_id = pn.paciente_id AND pp2.ativo = true) = 1),
            (SELECT v.usuario_id
               FROM vinculos v
              WHERE v.organizacao_id = p.organizacao_id AND v.papel = 'PROPRIETARIO' AND v.ativo = true
              ORDER BY v.id ASC
              LIMIT 1)
        ) AS avaliador_id,
        pn.atualizado_em::date AS data,
        pn.peso AS peso
    FROM perfis_nutricionais pn
    JOIN pacientes p ON p.id = pn.paciente_id
    WHERE pn.peso IS NOT NULL
      AND NOT EXISTS (SELECT 1 FROM avaliacoes a WHERE a.paciente_id = pn.paciente_id)
)
INSERT INTO avaliacoes (organizacao_id, paciente_id, avaliador_id, data, tipo, status, peso, percentual_gordura, criado_em, atualizado_em)
SELECT organizacao_id, paciente_id, avaliador_id, data, 'ANTROPOMETRIA', 'CONCLUIDA', peso, NULL, now(), now()
FROM candidatos
WHERE avaliador_id IS NOT NULL;
