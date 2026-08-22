-- Role.USER foi renomeado para Role.PROFESSIONAL no codigo (mesmo nivel de
-- acesso, nome mais correto para o dominio). A coluna "role" e VARCHAR (nao
-- ordinal), entao a migracao dos dados existentes e uma simples troca de
-- valor, sem impacto em id, FK ou qualquer outra coluna.
UPDATE users SET role = 'PROFESSIONAL' WHERE role = 'USER';
