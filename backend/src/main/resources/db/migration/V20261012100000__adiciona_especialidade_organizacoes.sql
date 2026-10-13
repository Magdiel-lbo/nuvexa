-- Vertical clínica da organização. Todas as organizações existentes são de nutrição (única vertical até aqui).
ALTER TABLE organizacoes ADD COLUMN especialidade VARCHAR(30) NOT NULL DEFAULT 'NUTRICAO';
