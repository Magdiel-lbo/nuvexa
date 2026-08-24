-- Traduz o esquema inteiro para português: tabelas, colunas, constraints,
-- índices, sequences e os valores dos enums já gravados.

-- ---------------------------------------------------------------------------
-- pacientes
-- ---------------------------------------------------------------------------
ALTER TABLE patients RENAME TO pacientes;
ALTER TABLE pacientes RENAME COLUMN name TO nome;
ALTER TABLE pacientes RENAME COLUMN birth_date TO data_nascimento;
ALTER TABLE pacientes RENAME COLUMN gender TO sexo;
ALTER TABLE pacientes RENAME COLUMN created_at TO criado_em;
ALTER TABLE pacientes RENAME COLUMN updated_at TO atualizado_em;
ALTER TABLE pacientes RENAME CONSTRAINT patients_pkey TO pacientes_pkey;
ALTER SEQUENCE patients_id_seq RENAME TO pacientes_id_seq;

UPDATE pacientes SET sexo = 'MASCULINO' WHERE sexo = 'MALE';
UPDATE pacientes SET sexo = 'FEMININO'  WHERE sexo = 'FEMALE';

-- ---------------------------------------------------------------------------
-- perfis_nutricionais
-- ---------------------------------------------------------------------------
ALTER TABLE nutrition_profiles RENAME TO perfis_nutricionais;
ALTER TABLE perfis_nutricionais RENAME COLUMN patient_id TO paciente_id;
ALTER TABLE perfis_nutricionais RENAME COLUMN goal TO objetivo;
ALTER TABLE perfis_nutricionais RENAME COLUMN activity_level TO nivel_atividade;
ALTER TABLE perfis_nutricionais RENAME COLUMN manual_daily_calories TO calorias_diarias_manuais;
ALTER TABLE perfis_nutricionais RENAME COLUMN height TO altura;
ALTER TABLE perfis_nutricionais RENAME COLUMN weight TO peso;
ALTER TABLE perfis_nutricionais RENAME COLUMN notes TO observacoes;
ALTER TABLE perfis_nutricionais RENAME COLUMN created_at TO criado_em;
ALTER TABLE perfis_nutricionais RENAME COLUMN updated_at TO atualizado_em;
ALTER TABLE perfis_nutricionais RENAME CONSTRAINT nutrition_profiles_pkey TO perfis_nutricionais_pkey;
ALTER TABLE perfis_nutricionais RENAME CONSTRAINT fk_nutrition_profiles_patient TO fk_perfis_nutricionais_paciente;
ALTER TABLE perfis_nutricionais RENAME CONSTRAINT uq_nutrition_profiles_patient_id TO uq_perfis_nutricionais_paciente_id;
ALTER TABLE perfis_nutricionais RENAME CONSTRAINT chk_nutrition_profiles_height_positive TO chk_perfis_nutricionais_altura_positiva;
ALTER TABLE perfis_nutricionais RENAME CONSTRAINT chk_nutrition_profiles_weight_positive TO chk_perfis_nutricionais_peso_positivo;
ALTER SEQUENCE nutrition_profiles_id_seq RENAME TO perfis_nutricionais_id_seq;

UPDATE perfis_nutricionais SET objetivo = 'EMAGRECIMENTO'          WHERE objetivo = 'LOSE_WEIGHT';
UPDATE perfis_nutricionais SET objetivo = 'MANUTENCAO_PESO'        WHERE objetivo = 'MAINTAIN_WEIGHT';
UPDATE perfis_nutricionais SET objetivo = 'GANHO_MASSA_MUSCULAR'   WHERE objetivo = 'GAIN_MUSCLE_MASS';
UPDATE perfis_nutricionais SET objetivo = 'CONDICIONAMENTO_FISICO' WHERE objetivo = 'IMPROVE_CONDITIONING';

UPDATE perfis_nutricionais SET nivel_atividade = 'SEDENTARIO'          WHERE nivel_atividade = 'SEDENTARY';
UPDATE perfis_nutricionais SET nivel_atividade = 'LEVEMENTE_ATIVO'     WHERE nivel_atividade = 'LIGHTLY_ACTIVE';
UPDATE perfis_nutricionais SET nivel_atividade = 'MODERADAMENTE_ATIVO' WHERE nivel_atividade = 'MODERATELY_ACTIVE';
UPDATE perfis_nutricionais SET nivel_atividade = 'MUITO_ATIVO'         WHERE nivel_atividade = 'VERY_ACTIVE';
UPDATE perfis_nutricionais SET nivel_atividade = 'EXTREMAMENTE_ATIVO'  WHERE nivel_atividade = 'EXTRA_ACTIVE';

-- ---------------------------------------------------------------------------
-- usuarios
-- ---------------------------------------------------------------------------
ALTER TABLE users RENAME TO usuarios;
ALTER TABLE usuarios RENAME COLUMN name TO nome;
ALTER TABLE usuarios RENAME COLUMN password TO senha;
ALTER TABLE usuarios RENAME COLUMN role TO perfil;
ALTER TABLE usuarios RENAME COLUMN enabled TO ativo;
ALTER TABLE usuarios RENAME COLUMN reset_token_hash TO hash_token_redefinicao;
ALTER TABLE usuarios RENAME COLUMN reset_token_expires_at TO token_redefinicao_expira_em;
ALTER TABLE usuarios RENAME COLUMN created_at TO criado_em;
ALTER TABLE usuarios RENAME COLUMN updated_at TO atualizado_em;
ALTER TABLE usuarios RENAME CONSTRAINT users_pkey TO usuarios_pkey;
ALTER TABLE usuarios RENAME CONSTRAINT uq_users_email TO uq_usuarios_email;
ALTER SEQUENCE users_id_seq RENAME TO usuarios_id_seq;

UPDATE usuarios SET perfil = 'PROFISSIONAL' WHERE perfil = 'PROFESSIONAL';

-- ---------------------------------------------------------------------------
-- organizacoes
-- ---------------------------------------------------------------------------
ALTER TABLE organizations RENAME TO organizacoes;
ALTER TABLE organizacoes RENAME COLUMN name TO nome;
ALTER TABLE organizacoes RENAME COLUMN legal_name TO razao_social;
ALTER TABLE organizacoes RENAME COLUMN document TO documento;
ALTER TABLE organizacoes RENAME COLUMN type TO tipo;
ALTER TABLE organizacoes RENAME COLUMN created_at TO criado_em;
ALTER TABLE organizacoes RENAME COLUMN updated_at TO atualizado_em;
ALTER TABLE organizacoes RENAME CONSTRAINT organizations_pkey TO organizacoes_pkey;
ALTER SEQUENCE organizations_id_seq RENAME TO organizacoes_id_seq;

UPDATE organizacoes SET tipo = 'CLINICA' WHERE tipo = 'CLINIC';

UPDATE organizacoes SET status = 'ATIVA'    WHERE status = 'ACTIVE';
UPDATE organizacoes SET status = 'SUSPENSA' WHERE status = 'SUSPENDED';
