# Checklist de testes manuais (Swagger UI)

Roteiro de cenários pra validar cada endpoint pela Swagger UI (`/swagger-ui.html`) no ambiente de
dev — complementa a coluna "Testado manualmente" do `SWAGGER_CHECKLIST.md` (marque lá quando um
endpoint estiver 100% coberto aqui). Cobre caminho feliz + os casos de erro/regra de negócio que
não são óbvios só lendo o schema — a maioria veio da auditoria feita pra escrever
`descriptions.yml` (ver também `BUGS_ENCONTRADOS.md` pros pontos já sinalizados como suspeita de
bug, marcados abaixo com ⚠️).

Pré-requisito: usuário autenticado (login via `POST /api/v1/auth/login`, usar o token no botão
"Authorize" do Swagger UI) e pelo menos 1 paciente e 1 profissional/vínculo cadastrados na
organização.

## Autenticação — `AutenticacaoController`

- [ ] `POST /auth/cadastrar` — cadastro válido retorna 201 + token
- [ ] `POST /auth/cadastrar` — e-mail já cadastrado retorna 409
- [ ] `POST /auth/cadastrar` — campo obrigatório faltando retorna 400
- [ ] `POST /auth/login` — credenciais válidas retornam 200 + token
- [ ] `POST /auth/login` — senha errada retorna 401
- [ ] `POST /auth/esqueci-senha` — e-mail cadastrado dispara envio (checar log/Brevo, resposta é sempre 200 mesmo se e-mail não existir — confirmar que não vaza se o e-mail existe)
- [ ] `POST /auth/redefinir-senha` — token válido troca a senha (200); token inválido/expirado retorna 400

## Contexto — `ContextoController`

- [ ] `GET /contexto` — retorna usuário/organização/papel do token autenticado

## Menus — `MenuController`

- [ ] `GET /menus` — retorna a árvore de navegação (comparar com `menu.yml`)

## Pacientes — `PacienteController`

- [ ] `GET /pacientes` — lista pacientes da organização
- [ ] `GET /pacientes?busca=` — filtra por nome (parcial, case-insensitive)
- [ ] `GET /pacientes/{id}` — paciente existente retorna 200
- [ ] `GET /pacientes/{id}` — id de outra organização ou inexistente retorna 404 (não 403 — checar que não vaza existência)
- [ ] `GET /pacientes/enums` — retorna opções de sexo/objetivo/nível de atividade
- [ ] `POST /pacientes` — cadastro válido retorna 201
- [ ] `POST /pacientes` — sem campo obrigatório retorna 400
- [ ] `PUT /pacientes/{id}` — atualização válida retorna 200
- [ ] `PUT /pacientes/{id}` — id inexistente retorna 404

## Relatório de pacientes — `PacienteRelatorioController`

- [ ] `GET /pacientes/relatorio` — JSON sem filtro retorna todas as linhas da organização
- [ ] `GET /pacientes/relatorio` — cada filtro (busca, sexo, objetivo, nivelAtividade) isolado reduz o resultado corretamente
- [ ] `GET /pacientes/relatorio/excel` — baixa `.xlsx` válido (abrir o arquivo, não só checar status 200)

## Perfil nutricional — `PerfilNutricionalController`

- [ ] `GET /perfis-nutricionais` — lista perfis; `busca` filtra por nome do paciente
- [ ] `GET /pacientes/{pacienteId}/perfil-nutricional` — paciente com perfil retorna 200
- [ ] `GET /pacientes/{pacienteId}/perfil-nutricional` — paciente sem perfil cadastrado retorna 404
- [ ] `GET /perfis-nutricionais/enums` — retorna opções de objetivo/nível de atividade
- [ ] `POST /pacientes/{pacienteId}/perfil-nutricional` — `altura` em metros (ex.: `1.75`, não `175`) — **confirmar que o IMC calculado bate** (regressão do exemplo adicionado no DTO)
- [ ] `POST .../perfil-nutricional` — `pesoInicial` informado vira a primeira Avaliação do paciente (conferir em `GET /avaliacoes?pacienteId=`)
- [ ] `POST .../perfil-nutricional` — paciente que já tem perfil: confirmar o que acontece (erro? sobrescreve? não documentado como erro específico — checar comportamento real)
- [ ] `POST .../perfil-nutricional` — paciente inexistente retorna 404
- [ ] `PUT .../perfil-nutricional` — atualização válida retorna 200; alteração de `altura`/`objetivo` reflete no cálculo de IMC/TMB/gasto calórico
- [ ] `DELETE .../perfil-nutricional` — remove o perfil e **preserva o paciente** (confirmar com `GET /pacientes/{id}` depois)

## Avaliações — `AvaliacaoController`

- [ ] `GET /avaliacoes` — lista; `pacienteId` e `busca` filtram corretamente
- [ ] `GET /avaliacoes/{id}` — existente retorna 200 com `variacaoPeso` calculada corretamente vs. avaliação anterior do mesmo paciente
- [ ] `GET /avaliacoes/{id}` — inexistente retorna 404
- [ ] `GET /avaliacoes/enums` — retorna tipo/status
- [ ] `POST /avaliacoes` — criação válida retorna 201
- [ ] `POST /avaliacoes` — `data` futura **com peso informado** retorna 400; `data` futura **sem peso** é aceita (checar os dois casos, a regra é condicional)
- [ ] `POST /avaliacoes` — `avaliadorId` de usuário sem vínculo ativo na organização retorna 400
- [ ] `POST /avaliacoes` — `pacienteId` inexistente retorna 404
- [ ] `PUT /avaliacoes/{id}` — atualização válida em avaliação não concluída retorna 200
- [ ] `PUT /avaliacoes/{id}` — avaliação com status `CONCLUIDA` retorna 400 (imutável)
- [ ] `PUT /avaliacoes/{id}` — tentar mudar `pacienteId` no corpo é ignorado/sem efeito (campo nem existe no DTO — confirmar que o paciente da avaliação não muda)
- [ ] ⚠️ `DELETE /avaliacoes/{id}` — **testar exclusão de avaliação `CONCLUIDA`**: hoje o código permite (achado #2 do `BUGS_ENCONTRADOS.md`); confirmar que realmente exclui sem erro, documentando a reprodução exata (id, status antes)
- [ ] `DELETE /avaliacoes/{id}` — inexistente retorna 404

## Relatório de avaliações — `AvaliacaoRelatorioController`

- [ ] `GET /avaliacoes/relatorio` — sem filtro retorna todas as linhas
- [ ] `GET /avaliacoes/relatorio?periodo=` — testar cada valor (hoje, 7d, 30d, 90d, ano) e confirmar a janela de datas
- [ ] `GET /avaliacoes/relatorio?tendencia=` — testar queda/estavel/alta e conferir contra o `variacaoPeso` de cada avaliação retornada
- [ ] `GET /avaliacoes/relatorio/excel` — baixa `.xlsx` válido

## Planos alimentares — `PlanoAlimentarController`

- [ ] `GET /planos-alimentares` — lista; `busca` filtra por **nome do plano OU nome do paciente** (testar os dois casos separadamente, é diferente da busca de Avaliação/Paciente)
- [ ] `GET /planos-alimentares/{id}` — existente retorna 200; inexistente retorna 404
- [ ] `GET /planos-alimentares/enums` — retorna status
- [ ] `POST /planos-alimentares` — criação válida (`calorias` e `refeicoesPorDia` positivos) retorna 201
- [ ] `POST /planos-alimentares` — `calorias` ou `refeicoesPorDia` ≤ 0 retorna 400
- [ ] `PUT /planos-alimentares/{id}` — plano `RASCUNHO` → `ATIVO` permitido
- [ ] `PUT /planos-alimentares/{id}` — plano `ATIVO` → `RASCUNHO` retorna 400 (transição proibida)
- [ ] `PUT /planos-alimentares/{id}` — plano `ENCERRADO` → qualquer alteração retorna 400 (imutável)
- [ ] ⚠️ `DELETE /planos-alimentares/{id}` — **testar exclusão de plano `ENCERRADO`**: hoje o código permite (achado #3 do `BUGS_ENCONTRADOS.md`); confirmar e documentar
- [ ] `DELETE /planos-alimentares/{id}` — inexistente retorna 404

## Relatório de planos alimentares — `PlanoAlimentarRelatorioController`

- [ ] `GET /planos-alimentares/relatorio?plano=` — confirmar que é **match exato** do nome (não parcial) — testar nome parcial (deve dar 0 resultados) e nome exato (deve retornar)
- [ ] `GET /planos-alimentares/relatorio?faixaCalorica=` — testar `ate1600`, `entre1600e2200`, `acima2200` e as fronteiras (1600 e 2200 exatos)
- [ ] `GET /planos-alimentares/relatorio/excel` — baixa `.xlsx` válido

## Consultas — `ConsultaController`

- [ ] `GET /consultas` — lista; `pacienteId` e `busca` filtram corretamente
- [ ] `GET /consultas/profissionais` — lista profissionais elegíveis (vínculo ativo na organização)
- [ ] `GET /consultas/{id}` — existente/inexistente (200/404)
- [ ] `POST /consultas` — criação válida retorna 201
- [ ] `POST /consultas` — segunda consulta no **mesmo horário para o mesmo profissional** retorna 400 (conflito)
- [ ] `POST /consultas` — segunda consulta no **mesmo horário para o mesmo paciente** (profissional diferente) retorna 400 (conflito)
- [ ] `POST /consultas` — consulta com status `CANCELADA` **não** deve gerar conflito mesmo em horário sobreposto (a checagem pula consultas canceladas)
- [ ] `POST /consultas` — sobreposição parcial de horário (não exata, ex.: consulta de 30min começando no meio de outra) também deve ser barrada
- [ ] `PUT /consultas/{id}` — consulta `REALIZADA`/`CANCELADA`/`FALTOU` retorna 400 em qualquer alteração (imutável)
- [ ] `PUT /consultas/{id}` — reagendar horário validando conflito de novo (excluindo a própria consulta da comparação)
- [ ] `DELETE /consultas/{id}` — remove consulta existente; inexistente retorna 404
- [ ] ⚠️ `DELETE /consultas/{id}` de consulta `REALIZADA`/`CANCELADA`/`FALTOU` — **suspeita de bug** (mesmo padrão de Avaliação/Plano Alimentar, ver nota de "padrão sistêmico" no `BUGS_ENCONTRADOS.md`; não confirmado ainda, checar e, se reproduzir, promover a achado formal)

## Relatório de consultas — `ConsultaRelatorioController`

- [ ] `GET /consultas/relatorio?periodo=` — testar cada valor e conferir a janela de datas
- [ ] `GET /consultas/relatorio?profissionalId=` — filtra corretamente por 1 e por múltiplos ids
- [ ] `GET /consultas/relatorio/excel` — baixa `.xlsx` válido

## Prontuários — `ProntuarioController`

- [ ] `GET /prontuarios` — lista; `pacienteId`/`busca` filtram corretamente
- [ ] `GET /prontuarios/{id}` — existente/inexistente (200/404)
- [ ] `GET /prontuarios/enums` — retorna seção/status/tipo de evento de auditoria
- [ ] `POST /prontuarios` — criação com status `RASCUNHO` ou `PENDENTE` retorna 201
- [ ] `POST /prontuarios` — criação com status `ASSINADO` **direto** retorna 400 (deve usar `/assinar`)
- [ ] `PUT /prontuarios/{id}` — atualização válida em prontuário não assinado retorna 200
- [ ] `PUT /prontuarios/{id}` — prontuário `ASSINADO` retorna 400 (imutável) mesmo mudando um campo qualquer
- [ ] `PUT /prontuarios/{id}` — tentar setar `status: ASSINADO` pelo PUT retorna 400 (mesma regra do POST)
- [ ] `PATCH /prontuarios/{id}/assinar` — prontuário com conteúdo preenchido: assina com sucesso (200), status vira `ASSINADO`, `assinadoPor`/`assinadoEm` preenchidos
- [ ] `PATCH .../assinar` — prontuário sem `conteudo` (vazio/branco) retorna 400
- [ ] `PATCH .../assinar` — prontuário já `ASSINADO` retorna 400
- [ ] `DELETE /prontuarios/{id}` — prontuário não assinado e **sem** adendo/anexo: exclui com sucesso (204)
- [ ] `DELETE /prontuarios/{id}` — prontuário `ASSINADO` retorna 400
- [ ] `DELETE /prontuarios/{id}` — prontuário com adendo ou anexo associado retorna 400 (mesmo não assinado) — testar os dois casos (só adendo, só anexo)
- [ ] `GET /prontuarios/{id}/auditoria` — retorna eventos na ordem certa, cobrindo criação/edição/assinatura; conferir `dadosAntes`/`dadosDepois`

## Adendos de prontuário — `ProntuarioAdendoController`

- [ ] `GET /prontuarios/{prontuarioId}/adendos` — lista adendos (vazio se não houver)
- [ ] `POST /prontuarios/{prontuarioId}/adendos` — prontuário `ASSINADO`: cria com sucesso (201)
- [ ] `POST .../adendos` — prontuário **não assinado** (RASCUNHO/PENDENTE) retorna 400 — **essa é a regra oposta à de Anexo, testar os dois de propósito pra não confundir**
- [ ] `POST .../adendos` — `texto` vazio/em branco retorna 400
- [ ] `POST .../adendos` — `autorId` inválido (sem vínculo ativo) retorna 400
- [ ] Confirmar que adendo criado aparece em `GET /prontuarios/{id}/auditoria` como evento `ADENDO`

## Anexos de prontuário — `ProntuarioAnexoController`

- [ ] `GET /prontuarios/{prontuarioId}/anexos` — lista anexos (vazio se não houver)
- [ ] `POST .../anexos` — upload de arquivo válido (PDF, JPG, PNG, DOC, DOCX, XLS ou XLSX, < 10MB) em prontuário **não assinado**: retorna 201
- [ ] `POST .../anexos` — upload em prontuário **assinado** retorna 400 (ler a mensagem de erro retornada — ⚠️ ver achado #4: o texto fala em "excluir", confirmar que a mensagem realmente sai errada nesse caso)
- [ ] `POST .../anexos` — arquivo vazio retorna 400
- [ ] `POST .../anexos` — arquivo > 10MB retorna 400 (testar tanto o limite do service quanto o do Spring/`MaxUploadSizeExceededException`, se der pra diferenciar)
- [ ] `POST .../anexos` — tipo MIME não permitido (ex.: `.txt`, `.zip`) retorna 400
- [ ] `POST .../anexos` — extensão não bate com um dos tipos permitidos retorna 400
- [ ] `GET .../anexos/{anexoId}/download` — baixa o arquivo com nome original e tipo MIME corretos (funciona mesmo com prontuário assinado — download não é bloqueado)
- [ ] `DELETE .../anexos/{anexoId}` — exclui anexo de prontuário não assinado (204)
- [ ] `DELETE .../anexos/{anexoId}` — prontuário assinado retorna 400
- [ ] Confirmar que upload/exclusão aparecem em `GET /prontuarios/{id}/auditoria` como eventos `UPLOAD_ANEXO`/`EXCLUSAO_ANEXO`

## Relatório de prontuários — `ProntuarioRelatorioController`

- [ ] `GET /prontuarios/relatorio?periodo=` — confirmar que filtra por **data de última atualização** (`atualizadoEm`), não pela data de criação — editar um prontuário antigo e ver se ele "sobe" pro filtro `hoje`
- [ ] `GET /prontuarios/relatorio?anexo=sim` e `?anexo=nao` — cada um isolado filtra corretamente
- [ ] `GET /prontuarios/relatorio?anexo=sim&anexo=nao` (as duas juntas) — confirmar que **não filtra nada** (equivale a omitir o parâmetro), conforme o código
- [ ] `GET /prontuarios/relatorio/excel` — baixa `.xlsx` válido

---

## Depois de rodar

- Marcar a coluna "Testado manualmente" de cada endpoint em `SWAGGER_CHECKLIST.md`.
- Qualquer discrepância encontrada (além das já sinalizadas com ⚠️) vai para `BUGS_ENCONTRADOS.md`,
  não corrigir no meio do teste.
- Os itens ⚠️ que confirmarem os achados #2/#3 (delete não bloqueando estado terminal) servem de
  evidência concreta pra quando formos revisar o padrão sistêmico — anotar o id/organização usados
  no teste, ajuda a reproduzir depois se for escrever um teste automatizado de regressão.
